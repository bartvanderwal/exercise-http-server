# Introductie

Deze uitwerking is een clone van de [originele repo](https://github.com/HANICA-DEA/exercise-http-server), met in de `master` branch een iets alternatieve uitwerking ter ilustratie voor geinteresseerden.

Deze oefening is deel van de DEA Course aan de Hogeschool Arnhem/Nijmegen. Onderwerp is het bekend
raken met het Http-protocol in een simpele Java-server.

Een Web server die op het internet gebruikt wordt om webpagina's te tonen, is feitelijk server-software die voldoet aan het Http-protocol. Hoe een internet 
protocol er uit zou moeten zien wordt besloten door een groep mensen, genaamd de Internet Engineering Task Force (IETF).
Deze besluiten worden uitgebracht als een *Request for Comments* (RFC). De RFC voor het Http-protocol kan hier gevonden worden: 
[RFC7230](https://datatracker.ietf.org/doc/html/rfc7230).

Hieronder volgen een aantal oefeningen, waarbij je in verschillende klassen wijzigingen moet maken. Zorg dat je tenminste 
na elke oefening de nodigde _refactors_ doet om de code overzichtelijk te houden.

# Oefeningen

Bekijk de startcode in de master-branch. De server is op te starten via de main-methode van de `HttpServer`
klasse. Zorg ervoor dat je de startcode op een plaats (directory) zet waar geen spaties in voorkomen.

Voer de main-methode uit. De Server is nu opgestart. Navigeer via je browser naar [http://localhost:8383/index.html](http://localhost:8383/index.html) 
om te zien wat de server teruggeeft. Mocht je een Connection-error krijgen in de log van IntelliJ bij het gebruiken van Chrome, 
probeer dan Edge of Firefox te gebruiken. Daarnaast kan het zijn dat je browser soms zijn cache gebruikt als je een request verstuurd,
waardoor de pagina niet laadt. In dat geval kan je met Ctrl + F5 de pagina verversen zonder cache.

## 1) Afhandelen meerdere requests

Bekijk de startcode in de klasse `HttpServer` en probeer te begrijpen op welk moment de server wacht op Connecties van
een client. Gebruik Google met de zoektermen "java server socket" om je analyse te ondersteunen.

Merk op dat er in de `serverSocket.accept()` methode wordt gewacht totdat er een request is gedaan. Dit kun je zien door
met de debugger een breakpoint te zetten op regel 25. Na het opstarten zal de applicatie pas dit breakpoint bereiken
als er een request is gedaan.

Wat opvalt is dat je Server momenteel maar één Http-request kan afhandelen. Pas dit aan zodat het mogelijk wordt
om meerdere Http-requests af te handelen.
* Gebruik je kennis van multithreading om de `HttpServer` klasse zo aan te passen dat ieder Http-request op
  een eigen `Thread` wordt afgehandeld.
* Pas de server zodanig aan dat de `Threads` in een oneindige lus worden opgestart. Op deze manier wordt de server
  niet meer afgesloten na afhandelen van een Http-request.

## 2) Correct afhandelen van het Http-request

Bekijk de klasse `ConnectionHandler`. Hierin kun je zien dat de server momenteel niks doet met de inhoud van het
Http-request en altijd dezelfde inhoud en status retourneert. Je kunt dit testen door een deel van de URL te wijzigen. 
Ook de URL [http://localhost:8383/what-a-terribly-bigoted-server.html](http://localhost:8383/what-a-terribly-bigoted-server.html) 
zal precies dezelfde content retourneren. In dit onderdeel zul je dit gaan verbeteren.

### 2.1) Toevoegen van een _index.html_

Een Http-response bevat altijd een _status-line_, een of meer Http-headers en optioneel een Http-body (de inhoud van het 
bericht). De body van de response staat nu nog hard-coded in de `ConnectionHandler`. We gaan deze body verplaatsen naar
een _index.html_ bestand, die we daarna zullen inlezen en retourneren via de response.
* Creëer de map _src/main/resources_. Dit is de default plaats binnen een Maven project om niet-Java bestanden te plaatsen
  die nodig zijn voor het correct functioneren van een applicatie. Hierbij kun je denken aan plaatjes, configuratiebestanden 
  of html-bestanden.
* Voeg in deze nieuwe map een submap _pages/_ toe en plaats hierin een _index.html_ met de volgende content

```html
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Simple Http Server</title>
    </head>
    <body>
        <h1>Hi DEA folks!</h1>
        <p>This is a simple line in html.</p>
    </body>
</html>

```

### 2.2) Retourneer _index.html_

Pas de `ConnectionHandler` zodanig aan dat voor de Http-body van het Http-request de file _index.html_ wordt gebruikt.
* Gebruik voor het inlezen van een bestand de onderstaande methode.
* Plaats deze in een aparte klasse genaamd `HtmlPageReader`.
* Merk op dat onderstaande methode verwacht dat het html-bestand in de map _pages/_ staat.
* Gebruik deze methode om nu de inhoud van _index.html_ te retourneren. De constante `HTTP_BODY` uit `ConnectionHandler`
  kun je nu verwijderen.

```java
public class HtmlPageReader {
    public String readFile(String filename) {
        var fullFileName = "pages/".concat(filename);
        try {
            ClassLoader classLoader = getClass().getClassLoader();

            var file = new File(classLoader.getResource(fullFileName).getFile()).toPath();

            var fileAsString = new String(Files.readAllBytes(file));

            return fileAsString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 2.3) Aanpassen _index.html_ en de Http-headers

Voeg nu wat extra inhoud toe aan je _index.html_, herstart je server en bekijk het resultaat. De inhoud zal nu
niet meer volledig gerenderd worden. Dit komt omdat de _Content-Length_ headerwaarde niet meer correct is.
De specificaties voor Http response headers vind je in de opeenvolgende [RFC7231](https://datatracker.ietf.org/doc/html/rfc7231).
* Voeg aan de klasse `HtmlPageReader` een methode toe die de correcte _content-length_ kan bepalen van een
  gegeven bestand. Gebruik bovenstaande RFC om te begrijpen hoe de _content-length_ moet worden berekend.
* Gebruik de nieuwe methode om programmatisch de correcte Http-headers te genereren.

### 2.4) Toevoegen correcte datum in de Http-header

De *Date* headerwaarde is op dit moment statisch. Vervang deze door de datum waarop een response verstuurd
wordt. Gebruik hierbij weer RFC7231 om te bepalen welk formaat deze headerwaarde moet hebben.

### 2.5) Retourneren van het correcte bestand

We gaan nu de afhandeling van de aangevraagde resource (bestand) verwerken. Merk op dat de Server nu altijd
dezelfde _index.html_ teruggeeft, los van de url van het daadwerkelijke request. Voeg de volgende functionaliteit toe:
* De eerste regel van een Http-request, de start-line, bevat de Http-methode (in dit geval: GET), de _request-target_
  (in dit geval het html-bestand) en de versie van het Http-protocol. Bekijk [RFC7230](https://tools.ietf.org/html/rfc7230) 
  voor meer informatie over de start-line.
* Zorg ervoor dat de `ConnectionHandler` van het Http-request de start-line gebruikt om te achterhalen welke _html_
  pagina geretourneerd moet worden.
* Voeg een tweede pagina, bijvoorbeeld _about.html_, toe en test of deze correct geretourneerd wordt door naar
  [http://localhost:8383/about.html](http://localhost:8383/about.html) te navigeren.
* Merk op dat het nu enkel mogelijk moet zijn bestaande pagina's op te vragen, de happy-flow.

### 2.6) Retourneren van de correcte Http-status

Momenteel wordt nog standaard de Http-statuscode _200_ geretourneerd. Zorg ervoor dat de `ConnectionHandler` de
correcte statuscode retourneert indien de _request-target_ niet bestaat. Zie voor de beschrijvingen van Http-statuscodes 
[RFC7231](https://datatracker.ietf.org/doc/html/rfc7231).
* Zorg ervoor dat de relevante methodes uit `HtmlPageReader` een _Exception_ gooien indien er een html-pagina geladen
  moet worden die niet bestaat.
* Gebruik deze exceptie in de `ConnectionHandler` om zowel de juiste Http-header, als Http-body te retourneren.

# Extra uitdaging

* Verbeter de foutafhandeling. Momenteel wordt enkel onderscheid gemaakt tussen 404 en 200. Voeg ook, op een correcte
  en zinnige manier ondersteuning voor de volgende Http-statuscodes toe:
  * 500 INTERNAL SERVER ERROR
  * 501 NOT IMPLEMENTED
  * 505 HTTP VERSION NOT SUPPORTED


* Het Http-protocol kent meer methodes dan enkel de *GET*. Lees [RFC7230](https://tools.ietf.org/html/rfc7230) voor meer 
  informatie en implementeer ook de methode *HEAD*.
