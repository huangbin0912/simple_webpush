/*
 @author erichuang
*/

this.com = this.com || {};
com.sohu = {};
com.sohu.video = {};

com.sohu.video.WebPush = function(configuration){
	var _configuration = configuration;
	//webpush comet urls
	var _webpushers = [];
	
	var _channel = "";
	var _subscription;
	var _self = this;
    var _connected = false;
    var _disconnecting;
	var _maxRetryCount = 5;
	var _retryCount = 0;
	var _topic = "";
	var _group = 1;
	var _active;
	var _cometdURL;
	var _pushComet;
	
	var _handshakeSub;
	var _connectSub;
	var _webpushNum = 0;
	
	_init();
	
	function _init(){
		if(_configuration){
		
			//add simplepush path
			if(_configuration.urls && _configuration.urls.length > 0){
				for(var index in _configuration.urls){
					var url = _configuration.urls[index];
					if(url && url.length > 0){
						if(url.charAt(url.length - 1) == '/'){
							url += "simplepush"
						}else{
							url += "/simplepush"
						}
					}
					_webpushers.push(url);
				}
			}
			
			if(_configuration.topic){
				_topic =_configuration.topic;
			}
			
			if(_configuration.group){
				_group = Math.ceil(Math.random()*_configuration.group);;
			}
			_channel = "/simplepush/" + _topic + "/" + _group;
			_active = true;
		}
	}
	
	this.CometD = null;
	
	this.join = function(){
		if(!_active){
			return;
		}
		_retryCount = 0;
		_disconnecting = false;
		if(_webpushNum >= _webpushers.length){
			_webpushNum = 0;
		}
		//get webpush comet url
		_cometdURL = _webpushers[_webpushNum];
		_webpushNum++;
		
		_self.CometD = new $.Cometd(_cometdURL);
		
		_handshakeSub = _self.CometD.addListener('/meta/handshake', _metaHandshake);
		_connectSub = _self.CometD.addListener('/meta/connect', _metaConnect);
		//config
		_self.CometD.configure({
			url: _cometdURL,
			logLevel: 'info',
			backoffIncrement : 0,
			maxNetworkDelay : 3000
		});
		// try to connect
		_self.CometD.handshake();
	}
	
	this.leave = function(){
		if(!_active){
			return;
		}
		_unsubscribe();
		_self.CometD.removeListener(_handshakeSub);
		_self.CometD.removeListener(_connectSub);
		_self.CometD.disconnect();
		_self.CometD = null;
		_disconnecting = true;
		_connectionClosed();
	}
	
	this.receive = function(msg){
		
	}
	
	function _unsubscribe(){
		if (_subscription){
			_self.CometD.unsubscribe(_subscription);
		}
		_subscription = null;
		
	}

	function _subscribe(){
		_subscription = _self.CometD.subscribe(_channel, _self.receive);
	}

	function _connectionInitialized(){
		// first time connection for this client, so subscribe tell everybody.
		_retryCount = 0;
		_self.CometD.batch(function(){
			_subscribe();
		});
	}

	function _connectionEstablished(){
		// connection establish (maybe not for first time), so just
		_retryCount = 0;
		_self.receive({
			data: {
				msg: 'Connection to Server ' + _cometdURL + ' Opened'
			}
		});
	}
	
	function _failover(){
	
		if(_retryCount >= _maxRetryCount){
			_self.leave();
			_self.join();
		}
		
	}
	
	function _connectionFailed(){
		_retryCount++;
		_self.receive({
			data: {
				msg: 'Connection to Server ' + _cometdURL + ' Failed'
			}
		});
		_failover();
	}

	function _connectionBroken(){
		_self.receive({
			data: {
				msg: 'Connection to Server ' + _cometdURL + ' Broken'
			}
		});
	}

	function _connectionClosed(){
		_connected = false;
	   _self.receive({
			data: {
				msg: 'Connection to Server ' + _cometdURL + ' Closed'
			}
		});
	}

	function _metaConnect(message){
		if (_disconnecting){
			_connectionClosed();
		}
		else{
			var wasConnected = _connected;
			_connected = message.successful === true;
			if (!wasConnected && _connected){
				_connectionEstablished();
			}
			else if (wasConnected && !_connected){
				_connectionBroken();
			}
			
			if(!_connected){
				_connectionFailed();
			}
		}
	}

	function _metaHandshake(message){
		if (message.successful){
			_connectionInitialized();
		}else{
			_connectionFailed();
		}
	}

	
}
