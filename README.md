EsupNfcTagServer is the server part of the EsupNfcTag project
============================

EsupNfcTagServer goal should be used with EsupNfcTagDrid (the Google Android App).

EsupNfcTagDrid allows the use of an android to swipe a tag. 

EsupNfcTagDrid can read directly the Tag Serial Number (UID) or read an identifiant protected by an AES Mifare Desfire Authentication.

The main part of the GUI is provided to EsupNfcTagDrid by EsupNfcTagServer inside a webview (standard web HTML application).
   
   
### Installation

EsupNfcTagServer needs Shibboleth to identify users.

You have to install a Shibboleth Provider on Apache (mod_shib) which proxy pass your EsupNfcTagServer web application.

/manager and /nfc must require shibboleth session : 

```
   <Location /manager>
     AuthType shibboleth
     ShibRequestSetting requireSession 1
     require shib-session
     ShibUseHeaders On
   </Location>

   <Location /admin>
     AuthType shibboleth
     ShibRequestSetting requireSession 1
     require shib-session
     ShibUseHeaders On
   </Location>

   <Location /nfc>
     AuthType shibboleth
     ShibRequestSetting requireSession 1
     require shib-session
     ShibUseHeaders On
   </Location>
```

   
### EsupNfcTagServer Test of the Android part without EsupNfcTagDrid


#### web part : 

For a first access, you can use :  
chrome http://esupnfctag.univ-ville.fr/nfc-index?apkVersion=1-2016-06-03-14-55-00&imei=123456

Next you can use the provided url (after auth part anbd redirections), that is to say : 
http://esupnfctag.univ-ville.fr/live?numeroId=6847041179388220887

#### nfc part : 

With CSN mode, you can call for example : 

```
curl -X POST -H "Content-type:application/json" -d '{"csn":"045371d2fd3a80","numeroId":"6847041179388220887"}' http://esupnfctag.univ-ville.fr/csn-ws
```

You have to know a valid CSN.
Take care to give a "LSB transformed csn" like given by usuals tag readers ...  
The numeroId is given by EsupNfcTagServer.

