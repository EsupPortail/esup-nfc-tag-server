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
	window.readyToScan = "ok";
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
	
	$(document).ready(function() {
		$('a[data-confirm]').click(function(ev) {
			var href = $(this).attr('href');
			if (!$('#dataConfirmModal').length) {
				$('body').append('<div id="dataConfirmModal" class="modal" role="dialog" aria-labelledby="dataConfirmLabel" aria-hidden="true"><div class="modal-header"><button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button><h3 id="dataConfirmLabel">Please Confirm</h3></div><div class="modal-body"></div><div class="modal-footer"><button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button><a class="btn btn-primary" id="dataConfirmOK">OK</a></div></div>');
			} 
			$('#dataConfirmModal').find('.modal-body').text($(this).attr('data-confirm'));
			$('#dataConfirmOK').attr('href', href);
			$('#dataConfirmModal').modal({show:true});
			return false;
		});
	});
	
	$(document).ready(function() {
	    $('#dialog').modal('hide');
	    $('#dialogfull').modal('hide');
	});

	$('#unregister').click(function(e) {
	    e.preventDefault();
	    var targetUrl = $(this).attr("href");
	    $("#displayModal").remove();
	    $('#dialogConfirmButton').on('click', function(event) {
	    	window.location.href = targetUrl;
		});
	    $('#dialogCancelButton').on('click', function(event) {
	    	$('#dialog').modal('hide');
		});
	    $('#dialog').modal('show');
	});

	$('#unregisterfull').click(function(e) {
	    e.preventDefault();
	    var targetUrl = $(this).attr("href");
	    $("#displayModal").remove();
	    $('#dialogFullConfirmButton').on('click', function(event) {
	        Android.disconnect();
	        window.location.href = targetUrl;
		});
	    $('#dialogFullCancelButton').on('click', function(event) {
	    	$('#dialogFull').modal('hide');
		});
	    $('#dialogFull').modal('show');
	});
	  
	/* MUSTACHE TEMPLATES */
	if(typeof numeroId != 'undefined'){
		if(numeroId!==""){
			$.Mustache.add('leoauth-template', '{{#leoauths}} \
					<tr id="row_{{id}}"> \
						<td class="td-live-text">{{firstname}} {{lastname}} <br/> {{authDateString}}<br/></td> \
						<td class="td-live-img"><div id="status_{{id}}">{{status}}</div></td> \
					</tr>{{/leoauths}}');
	
	
	
		}else{
			$.Mustache.add('leoauth-template', '{{#leoauths}} \
					<tr id="row_{{id}}"> \
						<td class="td-live-text">{{firstname}} {{lastname}} <br/> {{authDateString}}<br/></td> \
						<td class="td-live-text">{{location}}<br/></td> \
						<td class="td-live-img"><div id="status_{{id}}">{{status}}</div></td> \
					</tr>{{/leoauths}}');
		}
	}
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

	$.Mustache.add('display-template', '{{#display}} \
			<div id="displayModal" class="modal fade" role="dialog" style="width: 100%;"> \
				<div class="modal-dialog" > \
					<div class="modal-content"> \
						<!-- dialog body --> \
						<div class="modal-body" style="text-align: center"> \
							<h1>{{{display}}}</h1> \
							<button id="cancelButton}" type="button" data-dismiss="modal" class="btn btn-danger btn-lg">Fermer</button> \
						</div> \
					</div> \
				</div> \
			</div>{{/display}}');
	
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
	}
	


	/* LIVE LONG POLL */
	if(typeof numeroId != 'undefined') {

        var liveLongPoll = Object.create(esupLongPoll);


        liveLongPoll.load = function () {
            if (this.debug) {
                $('#status').text("Getting Taglogs...")
            }
            if (this.run) {
                $.ajax({
                    url: "/live/taglogs?authDateTimestamp=" + this.lastAuthDate + "&numeroId=" + numeroId,
                    context: this,
                    success: function (message) {

                        if (this.debug) {
                            $('#debug').text(JSON.stringify(message));
                        }
                        if (message && message.length) {
                            var newLastleoauth = $('#lastleoauth').mustache('leoauth-template', {'leoauths': message}, {method: 'prepend'});
                            var newleoauth = newLastleoauth.children('#newLog').fadeIn('slow').slice();
                            if (this.lastAuthDate == 0) {
                                newleoauth.attr("class", "oldLog");
                                if (message[0].liveStatus == "none" && message.length > 1) {
                                    message[0].liveStatus = "cancel"
                                }
                                if (message[0].status == "none" && message.length > 1) {
                                    message[0].status = "cancel"
                                }
                            }
                            var newValidateModal = $('#validate').mustache('validate-template', {'leoauths': message}, {method: 'prepend'});

                            if (message[0].liveStatus == "none" || message[0].status == "none") {
                                window.readyToScan = "ko";
                                if (validateAuthWoConfirmation == "true") {
                                    if (isDisplay == "true") {
                                        $("#displayModal").remove();
                                        $.get("/nfc-ws/display?id=" + message[0].id + "&numeroId=" + numeroId, function (display) {
                                            if (display != "" && display != "null") {
                                                $(".modal-backdrop.in").hide();
                                                var newDisplayModal = $('#display').mustache('display-template', {'display': display}, {method: 'prepend'});
                                                var displayModal = $('#displayModal').appendTo('body').modal({
                                                    backdrop: 'static',
                                                    keyboard: false,
                                                    show: true
                                                });
                                                getEsupNfcStorage().setItem("readyToScan", "ok");
                                                window.readyToScan = "ok";
                                                displayModal.on('hidden.bs.modal', function () {
                                                    $.get("/nfc-ws/dismiss?id=" + message[0].id + "&numeroId=" + numeroId, function (display) {
                                                    });
                                                });
                                            }
                                        });
                                    }
                                } else {
                                    $("#displayModal").remove();
                                    var validateModal = $('#validateModal').appendTo('body').modal({
                                        backdrop: 'static',
                                        keyboard: false,
                                        show: true
                                    });

                                    validateModal.on('hidden.bs.modal', function () {
                                        getEsupNfcStorage().setItem("readyToScan", "ok");
                                        window.readyToScan = "ok";
                                    });
                                    $('#validateButton_' + message[0].id).on('click', function (event) {
                                        $.get("/nfc-ws/validate?id=" + message[0].id + "&numeroId=" + numeroId, function (data) {
                                            if (data == true) {
                                                $('#status_' + message[0].id).html('<span class="icon-tag glyphicon glyphicon-ok-circle text-success"><!-- --></span>');
                                                $('#row_' + message[0].id).toggleClass("success");
                                                $.get("/nfc-ws/dismiss?id=" + message[0].id + "&numeroId=" + numeroId, function (display) {
                                                });
                                                if (isDisplay == "true") {
                                                    $.get("/nfc-ws/display?id=" + message[0].id + "&numeroId=" + numeroId, function (display) {
                                                        if (display != "" && display != "null") {
                                                            $(".modal-backdrop.in").hide();
                                                            var newDisplayModal = $('#display').mustache('display-template', {'display': display}, {method: 'prepend'});
                                                            var displayModal = $('#displayModal').appendTo('body').modal({
                                                                backdrop: 'static',
                                                                keyboard: false,
                                                                show: true
                                                            });
                                                            getEsupNfcStorage().setItem("readyToScan", "ok");
                                                            window.readyToScan = "ok";
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    });
                                    $('#cancelButton_' + message[0].id).on('click', function (event) {
                                        $(".modal-backdrop.in").hide();
                                        $.get("/nfc-ws/cancel?id=" + message[0].id + "&numeroId=" + numeroId, function (data) {
                                            if (data == true) {
                                                $('#status_' + message[0].id).html('<span class="icon-tag glyphicon glyphicon-remove-circle text-danger"><!-- --></span>');
                                                $('#row_' + message[0].id).toggleClass("danger");
                                                $.get("/nfc-ws/dismiss?id=" + message[0].id + "&numeroId=" + numeroId, function (display) {
                                                });
                                            }
                                        });
                                    });
                                }
                            }
                            this.lastAuthDate = message[0].authDate;
                            setTimeout(function () {
                                newleoauth.attr("class", "oldLog");
                            }, 2000);
                            var oldleoauth = $('#lastleoauth tr:gt(9)').slice();
                            setTimeout(function () {
                                oldleoauth.hide('slow', function () {
                                    oldleoauth.remove();
                                });
                            }, 5000);
                            try {
                                if (validateAuthWoConfirmation == "true") {
                                    getEsupNfcStorage().setItem("readyToScan", "ok");
                                    window.readyToScan = "ok";
                                }
                            } catch (e) {
                                if (this.debug) $('#status').text(e);
                            }
                        }
                        $("div[id^=status]").each(function () {
                            if ($(this).text() == "valid") {
                                $(this).html('<span class="icon-tag glyphicon glyphicon-ok-circle text-success"><!-- --></span>')
                            }
                            if ($(this).text() == "cancel") {
                                $(this).html('<span class="icon-tag glyphicon glyphicon-remove-circle text-danger"><!-- --></span>')
                            }
                            if ($(this).text() == "none") {
                                $(this).html('<span class="icon-tag glyphicon glyphicon-ban-circle text-warning"><!-- --></span>')
                            }
                        });
                        this.timer = this.poll();
                    },
                    error: function () {
                        if (this.debug) $('#status').text("Failed to get tagLogs");
                        setTimeout(function () {
                                if(typeof liveLongPoll != 'undefined') {
                                    liveLongPoll.timer = liveLongPoll.poll();
                                }
                        }, 2000);
                    },
                    cache: false
                })
                $('#status').text("");
            } else {
                if (this.debug) $('#status').text("Stopped");
            }
        }

    }
	
	$(function() {
		$.ajaxSetup({cache:false});
		if($('#lastleoauth').length) {
	        try{
	            getEsupNfcStorage().setItem("serviceUrl", serviceUrl);
	            getEsupNfcStorage().setItem("authType", authType);
	            getEsupNfcStorage().setItem("readyToScan", "ok");
	            window.readyToScan = "ok";
	        }catch(e){
	        	if(this.debug) document.getElementById("debug").innerHTML = e;
	        }
            if(typeof liveLongPoll != 'undefined') {
                liveLongPoll.start();
            }
		}
	});
	
	
	/* ERRORS LONG POLL */
	if(typeof numeroId != 'undefined') {
        var errorsLongPool = Object.create(esupLongPoll);

        errorsLongPool.load = function () {

            if (this.run) {

                $.ajax({
                    url: "/live/tagerror?errorDateTimestamp=" + this.lastAuthDate + "&numeroId=" + numeroId,
                    context: this,
                    success: function (message) {
                        if (this.debug) {
                            $('#debug').text(JSON.stringify(message))
                        }
                        if (message && message.length) {
                            window.readyToScan = "ko";
                            var newErrorModal = $('#error').mustache('error-template', {'tagerrors': message[0]}, {method: 'prepend'});
                            var errorModal = $('#errorModal').appendTo('body').modal({
                                backdrop: 'static',
                                keyboard: false,
                                show: true
                            });
                            errorModal.on('hidden.bs.modal', function () {
                                getEsupNfcStorage().setItem("readyToScan", "ok");
                                window.readyToScan = "ok";
                            });
                            this.lastAuthDate = message[0].errorDate;
                        }
                        this.timer = this.poll();
                    },
                    error: function () {
                        if (this.debug) $('#status').text("Failed to get errors");
                        setTimeout(function () {
                                if(typeof errorsLongPool != 'undefined') {
                                    errorsLongPool.timer = errorsLongPool.poll();
                                }
                        }, 2000);
                    },
                    cache: false
                });
            }
        }
    }

	$(function() {
		$.ajaxSetup({cache:false});
		if($('#lastleoauth').length) {
            if(typeof errorsLongPool != 'undefined') {
                errorsLongPool.start();
            }
		}
	});
	
});

$(document).ready(function(){
	var string = document.location.href;
	substring = "stats";
	if(string.indexOf(substring) > -1){
	Chart.defaults.global.responsive= true;
	Chart.defaults.global.maintainAspectRatio=false;
	Chart.defaults.global.responsiveAnimationDuration=0;
	Chart.defaults.global.legend.labels.fontFamily = "Arial";
	Chart.defaults.global.legend.labels.boxWidth = 20;
	Chart.defaults.global.legend.position= "bottom";
	Chart.defaults.global.title.fontFamily = "Arial";
	Chart.defaults.global.title.fontSize = 14;
	Chart.defaults.global.title.display= false;
	Chart.defaults.global.elements.point.radius=6;
	Chart.defaults.global.elements.line.tension=0.2;

		$.ajax({
			url: statsUrl + "/chartJson?model=numberTagByApplication&annee="+annee,
			type: 'GET',
			dataType : 'json',
			success : function(data) {
				var ctx = document.getElementById("tagsByApplication");
				var repartionComposantesChart = new Chart(ctx, {
					type: 'pie',
					data: data,
					options: {
						legend:{
							display:true
						},
						animation:{
							duration: 0,
						},
						hover: {
							animationDuration: 0, // duration of animations when hovering an item
						},
						responsiveAnimationDuration: 0,
						title: {
							text: "tagsByApplication",
						},
						tooltips: {
							callbacks: {
								label:function(item, data){
									console.log(data);
									var sum = 0;
									data.datasets[0].data.forEach(
										function addNumber(value) { sum += parseInt(value); }
									);
									pourcent = data.datasets[0].data[item.index] / sum * 100;
									return data.labels[item.index]+" : "+data.datasets[0].data[item.index] + " - "+ Math.round(pourcent*100)/100 +"%";
								}

							}
						}
					}
				});

			}
		});

		$.ajax({
			url: statsUrl + "/chartJson?model=numberTagByYear",
			type: 'GET',
			dataType : 'json',
			success : function(data) {
				var ctx = document.getElementById("tagByYear");
				var repartionComposantesChart = new Chart(ctx, {
					type: 'line',
					data: data,
					options: {
						responsive: true,
						scales: {
							yAxes: [{
								stacked: true
							}]
						}
					}
				});

			}
		});

	$.ajax({
        url: statsUrl + "/chartJson?model=numberDeviceByUserAgent&annee="+annee,
        type: 'GET',
        dataType : 'json',
        success : function(data) {
        	var ctx = document.getElementById("deviceByUserAgent");
        	var repartionComposantesChart = new Chart(ctx, {
        		type: 'pie',
        		data: data,
        		options: {
        			legend:{
        				display:true
        			},
            		 animation:{
             			 duration: 0,
             		 },        		
                     hover: {
                         animationDuration: 0, // duration of animations when hovering an item
                     },
                     responsiveAnimationDuration: 0,
                     title: {
                    	 text: "deviceByUserAgent",
                     },
                     tooltips: {
                         callbacks: {
                             label:function(item, data){
                            	 console.log(data);
                            	 var sum = 0;
                            	 data.datasets[0].data.forEach(
                            	     function addNumber(value) { sum += parseInt(value); }
                            	 );
                            	 pourcent = data.datasets[0].data[item.index] / sum * 100;
                            	 return data.labels[item.index]+" : "+data.datasets[0].data[item.index] + " - "+ Math.round(pourcent*100)/100 +"%";
                             }
                         
                         }
                     }
                }
        	});
        	
        }
	});

	$.ajax({
        url: statsUrl + "/chartJson?model=numberTagByLocation&annee="+annee+"&application="+application,
        type: 'GET',
        dataType : 'json',
        success : function(data) {
        	var ctx = document.getElementById("tagsByLocation");
        	var repartionComposantesChart = new Chart(ctx, {
        		type: 'horizontalBar',
        		data: data,
        		options: {
        	         legend: {
        	             display: false,
        	             position: 'right'
        	          },
             		 animation:{
             			 duration: 0,
             		 },        		
                     hover: {
                         animationDuration: 0, // duration of animations when hovering an item
                     },
                     responsiveAnimationDuration: 0,
        		tooltips: {
                    callbacks: {
                        label:function(item, data){
                       	 var sum = 0;
                       	 data.datasets[0].data.forEach(
                       	     function addNumber(value) { sum += parseInt(value); }
                       	 );
                       	 pourcent = data.datasets[0].data[item.index] / sum * 100;
                       	 return data.labels[item.index]+" : "+data.datasets[0].data[item.index] + " - "+ Math.round(pourcent*100)/100 +"%";
                        }
                    
                    }
                }
                },
                
        	});
        }
	
	});

	
	
	$.ajax({
        url: statsUrl + "/chartJson?model=nbTagThisDay&annee="+annee,
        type: 'GET',
        dataType : 'json',
        success : function(data) {
        	var ctx = document.getElementById("nbTagThisDay").getContext("2d");
        	var repartionComposantesChart = new Chart(ctx, {
        		type: 'doughnut',
        		data: data,
        		options: {
        			legend:{
        				display:true
        			},
                     title: {
                    	 text: "nbTagThisDay"
                     },
             		 animation:{
             			 duration: 0,
             		 },        		
                     hover: {
                         animationDuration: 0, // duration of animations when hovering an item
                     },
                     responsiveAnimationDuration: 0,
             		 tooltips: {
                        callbacks: {
                            label:function(item, data){
                           	 return data.labels[item.index]+" : "+data.datasets[0].data[item.index] + "%";
                            }
                        
                        }
                    }
                },

        	});
        }
	});
	
	$.ajax({
		url: statsUrl + "/chartJson?model=numberTagByWeek&annee="+annee,
        type: 'GET',
        dataType : 'json',
        success : function(data) {
        	var ctx = document.getElementById("tagsByWeek").getContext("2d");
        	var repartionComposantesChart = new Chart(ctx, {
        		type: 'bar',
        		data: data,
        		options: {
             		 animation:{
             			 duration: 0,
             		 },        		
                     hover: {
                         animationDuration: 0, // duration of animations when hovering an item
                     },
                     responsiveAnimationDuration: 0,
                }
        	});
        }
	});
	
	
	}
});


$(document).ready(function() {
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
				eppnInit : {
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
	
	$(function(){

		$("#application").focusout(function(){
			checkLocations();
		});
		$("#eppnInit").focusout(function(){
			checkLocations();
		});
		
		function checkLocations(){
			$('#location').val('');
			$('#location').find('option').remove().end();
			var eppn = $('#eppnInit').val();
			var idApp = $('#application').val();
    		$('#validateAuthWoConfirmation').prop('checked', false);
			var url = '/manager/devices/getValidateWo?applicationId='+idApp;
			$.ajax({
			    url: url,
			    type:'GET',
			    dataType: 'json',
			    success: function( json ) {
			    	if(json == true){
			    		$('#validateAuthWoConfirmation').prop('checked', true);
			    	}
			    }
			});
			
			if(eppn!=null && idApp!=null){
				$('#location').empty().append($('<option>').text("").attr('value', null));
				$("#loading").css("display", "block");
				var url = '/manager/devices/locationsJson?eppn='+eppn+'&applicationId='+idApp;
				$.ajax({
				    url: url,
				    type:'GET',
				    dataType: 'json',
				    success: function( json ) {
				    	$("#loading").css("display", "none");
				    	var count = Object.keys(json).length;
				    	if(count > 0) {
				    		$('#location').empty()
					        $.each(json, function(i, value) {
					            $('#location').append($('<option>').text(value).attr('value', value));
					        });
					        $("#location").click();
				    	}else{
				    		$('#location').empty().append($('<option>').text("").attr('value', null));
				    		$('#location').attr("title", "ERROR : no location found for this eppn / application");
			    			$('#device').submit();
				    	}
				    }
				});
			}
			
		}
	});
	
});

