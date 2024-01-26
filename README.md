Esup NFC Tag Server - EsupPortail
=================================

Application permettant d'utiliser comme lecteur/borne de badge NFC :

- un smartphone Android (EsupNfcTagDroid : https://github.com/EsupPortail/esup-nfc-tag-droid)
- ou ordinateur + lecteur usb NFC (EsupNfcTagDesktop : https://github.com/EsupPortail/esup-nfc-tag-desktop)
- ou encore éventuellement un Arduino (EsupNfcTagArduino : https://github.com/EsupPortail/esup-nfc-tag-arduino) 

Ce projet vise à permettre et faciliter le développement de services autour des cartes NFC dites "multiservice"

Il propose une architecture standardisée et connectée autour du badgeage d'une carte présentant un identifiant (CSN ou identifiant codé en Desfire AES) correspondant à une carte valide d'un individu connu du système d'information.

L'application EsupNfcTagServer, elle est développée en Spring et tourne sur Tomcat.


## Installation

### Pré-requis
* Java - JDK : OpenJDK 8 ou 11 ou 17 : le mieux est de l'installer via le système de paquets de votre linux.
* Maven : le mieux est de l'installer via le système de paquets de votre linux.
* Postgresql 9 ou > : le mieux est de l'installer via le système de paquets de votre linux.
* Tomcat (Tomcat 9)
* Apache + libapache2-mod-shib2 : https://services.renater.fr/federation/docs/installation/sp
* Git

### Configuration Apache Shibboleth 
L'authentification repose sur Shibboleth. Apache doit être configuré pour faire du mod_shib.

Une fois le SP Shibboleth et Apache configurés usuellement (voir : https://services.renater.fr/federation/docs/installation/sp), il faut sécuriser /manager et /nfc en ajoutant ceci à la conf apache (à adapter cependant en fonction des versions d'Apache et mod_shib) :

```
   <Location /manager>
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

### Configuration PostgreSQL

* pg_hba.conf : ajout de
```
host all all 127.0.0.1/32 password
```

* redémarrage de postgresql
* psql
```
create database esupnfctag;
create USER esupnfctag with password 'esup';
grant ALL ON DATABASE esupnfctag to esupnfctag;
ALTER DATABASE esupnfctag OWNER TO esupnfctag;
```

### Paramétrage mémoire JVM :

Pensez à paramétrer les espaces mémoire JVM : 
```
export JAVA_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256m"
```

Pour maven :
```
export MAVEN_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256m"
```

### Recupération des sources

```
git clone https://github.com/EsupPortail/esup-nfc-tag-server
```

### Obtention du war pour déploiement sur tomcat ou autre :
```
mvn clean package
```

### Lancement de la mise à jour de la base de données
```
mvn exec:java -Dexec.args="dbupgrade"
```

## Configuration

Voir la page wiki Esup : https://www.esup-portail.org/wiki/display/ESUPNFC/ESUP-NFC-TAG-SERVER

