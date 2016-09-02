/*
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
$(document).ready(function() {
	
	// Hack to test with a simple browser (without Android phone)
	window.getEsupNfcStorage = function() {
		try {
			AndroidLocalStorage.setItem("dummy", "dummy");
			return AndroidLocalStorage;
		} catch(e) {
			if (e instanceof ReferenceError) {
				return sessionStorage;
			} else {
			    throw e; 
			}
		}
	}

	window.androidLogout = function(urldisconnect) {
		if(confirm(msg)) {
			$.ajax({
				url : urldisconnect,
				context: this,
				success : function() {
					Android.disconnect();
				},
				cache : false
				});
			return true;
		}
		return false;
	}
	
	/* MUSTACHE TEMPLATES */
	
	$.Mustache.add('leoauth-template', '{{#leoauths}} \
			<tr id="row_{{id}}"> \
				<td class="td-live-text">{{firstname}} {{lastname}} <br/> {{authDateString}}<br/></td> \
				<td class="td-live-img"><div id="status_{{id}}">{{status}}</div></td> \
			</tr>{{/leoauths}}');

	$.Mustache.add('validate-template', '{{#leoauths}} \
		<div id="validateModal" class="modal fade" role="dialog" style="background-color: rgba(115, 210, 22, 0.5);"> \
			<div class="modal-dialog" > \
				<div class="modal-content"> \
					<!-- dialog body --> \
					<div class="modal-body" style="text-align: center"> \
						<h1>Confirmer le badgeage de {{firstname}} {{lastname}}</h1> \
						<button id="validateButton_{{id}}" type="button" data-dismiss="modal" class="btn btn-success btn-lg">Valider</button> \
						<button id="cancelButton_{{id}}" type="button" data-dismiss="modal" class="btn btn-danger btn-lg">Annuler</button> \
					</div> \
				</div> \
			</div> \
		</div>{{/leoauths}}');
	
	$.Mustache.add('error-template', '{{#tagerrors}} \
			<div id="errorModal" class="modal fade" role="dialog" style="background-color: rgba(255, 99, 99, 0.5);"> \
				<div class="modal-dialog" > \
					<div class="modal-content"> \
						<!-- dialog body --> \
						<div class="modal-body" style="text-align: center"> \
							<h1>{{exception.message}}</h1> \
							<button id="cancelButton" type="button" data-dismiss="modal" class="btn btn-danger btn-lg">Fermer</button> \
						</div> \
					</div> \
				</div> \
			</div>{{/tagerrors}}');
	
	/* ESUP LONG POLL PROTOTYPE */
	
	var esupLongPoll = {
			debug : false,
			run : false,
			timer : undefined,
			lastAuthDate : 0,
			list : undefined
	};
	esupLongPoll.start = function() {
		if (!this.run) {
			this.run = true;
			this.timer = this.poll();
		}
	}
	esupLongPoll.clear = function() {
		$('#lastleoauth').html('');
	}
	esupLongPoll.stop = function() {
		if (this.run && this.timer != null) {
			clearTimeout(this.timer);
		}
		run = false;		
	}
	esupLongPoll.poll = function() {
		if (this.timer != null) {
			clearTimeout(this.timer);
		}
		return $(this).delay(1000).load(); 
		//return setTimeout(this.load, 1000);
	}
	
	

	/* LIVE LONG POLL */
	
	var liveLongPoll = Object.create(esupLongPoll);

	liveLongPoll.load = function() {
		if(this.debug) {
			$('#status').text("Getting Taglogs...")
		}
		if (this.run) {
			$.ajax({
				url : "/live/taglogs?authDateTimestamp=" + this.lastAuthDate + "&numeroId=" + numeroId,
				context: this,
				success : function(message) {
					if (this.debug) {
						$('#debug').text(JSON.stringify(message))
					}
					if (message && message.length) {
						var newLastleoauth = $('#lastleoauth').mustache('leoauth-template', {'leoauths' : message}, { method: 'prepend' });
						var newleoauth = newLastleoauth.children('#newLog').fadeIn('slow').slice();	
						if(this.lastAuthDate==0) {
							newleoauth.attr("class", "oldLog");
							if(message[0].status == "none" && message.length>1){message[0].status = "cancel"}
						}
						var newValidateModal = $('#validate').mustache('validate-template', {'leoauths' : message}, { method: 'prepend' });
						if(message[0].status == "none"){
							var validateModal = $('#validateModal').appendTo("body").modal({backdrop: 'static', keyboard: false, show: true});						
							validateModal.on('hidden.bs.modal', function () {
								getEsupNfcStorage().setItem("readyToScan", "ok");
							});
							$('#validateButton_'+message[0].id).on('click', function(event) {
								$.get( "/nfc-ws/validate?id="+message[0].id, function( data ) {
									if(data==true){
										$('#status_'+message[0].id).html('<span class="icon-tag glyphicon glyphicon-ok-circle text-success"><!-- --></span>');
										$('#row_'+message[0].id).toggleClass("success");
									}
								});
							});
							$('#cancelButton_'+message[0].id).on('click', function(event) {
								$.get( "/nfc-ws/cancel?id="+message[0].id, function( data ) {
									if(data==true){
										$('#status_'+message[0].id).html('<span class="icon-tag glyphicon glyphicon-remove-circle text-danger"><!-- --></span>');
										$('#row_'+message[0].id).toggleClass("danger");
									}
								});
								
							});
						}
						this.lastAuthDate = message[0].authDate;
						setTimeout(function(){
							newleoauth.attr("class", "oldLog");
						}, 2000);
						var oldleoauth = $('.leo-old:gt(10)').slice();
						setTimeout(function(){
							oldleoauth.hide('slow', function(){ oldleoauth.remove(); });
						}, 5000);
						try{
							if(validateAuthWoConfirmation=="true"){
								getEsupNfcStorage().setItem("readyToScan", "ok");
							}
						}catch(e){
							if(this.debug) $('#status').text(e);
						}
					}
					$("div[id^=status]").each(function(){
						if($(this).text()=="valid"){$(this).html('<span class="icon-tag glyphicon glyphicon-ok-circle text-success"><!-- --></span>')}
						if($(this).text()=="cancel"){$(this).html('<span class="icon-tag glyphicon glyphicon-remove-circle text-danger"><!-- --></span>')}					
						if($(this).text()=="none"){$(this).html('<span class="icon-tag glyphicon glyphicon-ban-circle text-warning"><!-- --></span>')}
					});
					this.timer = this.poll();
				},
				error : function() {
					if(this.debug) $('#status').text("Failed to get tagLogs");
					this.timer = this.poll();
				},
				cache : false
			})
			$('#status').text("");
		} else {
			if(this.debug) $('#status').text("Stopped");
		}
	}
	
	
	$(function() {
		$.ajaxSetup({cache:false});
		if($('#lastleoauth').length) {
	        try{
	            getEsupNfcStorage().setItem("serviceUrl", serviceUrl);
	            getEsupNfcStorage().setItem("authType", authType);
	            getEsupNfcStorage().setItem("readyToScan", "ok");
	        }catch(e){
	        	if(this.debug) document.getElementById("debug").innerHTML = e;
	        }
			liveLongPoll.start();
		}
	});
	
	
	/* ERRORS LONG POLL */
	
	var errorsLongPool = Object.create(esupLongPoll);
	
	errorsLongPool.load = function() {

		if(this.run) {
			
			$.ajax({
				url : "/live/tagerror?errorDateTimestamp=" + this.lastAuthDate +"&numeroId=" + numeroId,
				context: this,
				success : function(message) {
					if (this.debug) {
						$('#debug').text(JSON.stringify(message))
					}
					if (message && message.length) {
						var newErrorModal = $('#error').mustache('error-template', {'tagerrors' : message[0]}, { method: 'prepend' });
						var errorModal = $('#errorModal').appendTo("body").modal({backdrop: 'static', keyboard: false, show: true});
						errorModal.on('hidden.bs.modal', function () {
							getEsupNfcStorage().setItem("readyToScan", "ok");
						});
						this.lastAuthDate = message[0].errorDate;
					}
					this.timer = this.poll();			
				},
				error : function() {
					if(this.debug) $('#status').text("Failed to get errors");
					this.timer = this.poll();
				},
				cache : false
			});
		}
	}
	
	$(function() {
		$.ajaxSetup({cache:false});
		if($('#lastleoauth').length) {
			errorsLongPool.start();
		}
	});
	
	/* USUAL FORM RULES */
	
	$(function(){
		
		$('#application').validate({
			rules : {
				name : {
					required : true
				},
				nfcConfig : {
					required : true
				},
				appliExt : {
					required : true
				},
				tagIdCheck : {
					required : true
				}
			}
		});
		
		$('#device').validate({
			rules : {
				numeroId : {
					required : true
				},
				application : {
					required : true
				},
				location : {
					required : true
				}
			}
		});
	});
	
});
