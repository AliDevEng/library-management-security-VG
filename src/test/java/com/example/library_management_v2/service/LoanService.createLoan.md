# Guide: Testning av LoanService - Från Grunden Till Expertis

## Varför testa LoanService?

Vi vill testa `LoanService.createLoan()` som är hjärtat i vårt bibliotekssystem. 
Tänk på denna metod som dirigenten i en orkester - den måste koordinera många olika delar för att skapa harmoni. 
När någon vill låna en bok händer följande kritiska saker:

- Systemet måste kontrollera att användaren finns
- Systemet måste kontrollera att boken finns
- Systemet måste kontrollera att boken är tillgänglig
- Systemet måste minska antalet tillgängliga exemplar
- Systemet måste skapa ett nytt lån med rätt datum

Om något av detta går fel kan hela bibliotekssystemet bli inkonsistent. 
Det är som om dirigenten missar en takt - hela orkestern kan komma ur synk. 
Därför behöver vi testa alla dessa steg noggrant för att vara säkra på att vår "dirigent" alltid gör rätt sak.

## Vad är Mock-objekt och varför använder vi dem?

Av säkerhetsskäl vill vi inte testa med en verklig databas eftersom datan inuti kan ändras under vårt test, 
och det vill vi absolut inte. Dessutom kanske vår databas inte ens är färdig än när vi utvecklar. 
Därför är det utmärkt att använda Mock-objekt.

Tänk dig att du vill testa en bil, men du har inte råd med bensin eller vill inte köra på riktiga vägar 
med riktig trafik. Då bygger du en simulator som låtsas vara vägar och bensin. 
Mock-objekt fungerar likadant - de låtsas vara databaser utan att faktiskt vara det. De ger oss 
full kontroll över vad som händer, precis som en körsimulator låter oss testa att köra i snöstorm utan att 
faktiskt behöva vänta på snö.

Mock-objekt ger oss en fejk-databas som är perfekt att använda vid testning. 
Vi kan programmera den att bete sig exakt som vi vill, vilket betyder att vi kan testa 
alla möjliga scenarion utan att behöva sätta upp komplicerade databaser med specifik data.

### Förklaringar av annotationer

`@ExtendWith(MockitoExtension.class)` säger åt JUnit att använda Mockito för att hantera våra mock-objekt. 
Det är som att säga åt en teaterregissör att använda marionettdockor istället för riktiga skådespelare för vissa roller.

`@Mock` skapar en "fejk" version av repository som vi kan programmera att bete sig som vi vill. 
Det är som att träna en papegoja att säga specifika fraser när du ställer specifika frågor.

`@InjectMocks` skapar en riktig LoanService men injicerar våra mock-repositories i den. Det är som att bygga en 
riktig bil men sätta in en simulator istället för en riktig motor - bilen fungerar fortfarande, 
men motorn är kontrollerad av oss.

## Förbereda testdata med @BeforeEach

Nästa steg är att förbereda testdata. Varför gör vi detta i en separat metod? Tänk dig att du är en kock 
som ska laga flera olika rätter under en kväll. Istället för att förbereda ingredienserna från början för varje rätt, 
förbereder du allt en gång på ett rent arbetsbord och använder sedan ingredienserna för olika recept.

På samma sätt förbereder vi testdata en gång och återanvänder den i olika tester. Men här kommer det viktiga - 
det är som att städa köket helt och hållet mellan varje rätt, så att smakerna från förra rätten inte påverkar nästa. 
På samma sätt städar `@BeforeEach` bort alla spår från föregående test.

`@BeforeEach` är magiskt - denna metod körs automatiskt före varje enskilt test. Det betyder att varje test 
börjar med "färska" objekt och inte påverkas av vad som hände i tidigare tester. Det är som att ha en tidsmaskin 
som återställer allt till utgångsläget innan varje experiment.

För att utföra ett lån behöver vi ha en "User", "Author" och "Book". Vi skapar dessa i vår `setUp()` metod, 
ungefär som en filmproducent förbereder alla rekvisita innan inspelningen börjar.

## AAA-mönstret: Vårt test-recept

När vi skriver tester följer vi det så kallade Triple A-mönstret - Arrange, Act, Assert. Det är som 
ett recept för framgångsrik testning:

**Arrange (Förbered):** Förbered allt som behövs för testet. Det är som att ställa fram alla ingredienser, 
värma ugnen och ta fram rätt redskap innan du börjar laga mat.

**Act (Agera):** Utför den funktion eller handling vi vill testa. Det är som att faktiskt blanda ihop ingredienserna 
och stoppa in kakan i ugnen.

**Assert (Bekräfta):** Kontrollera att resultatet blev som vi ville. Det är som att smaka på kakan och kontrollera att
den har rätt konsistens, smak och utseende.

Precis som i ett laboratorium där du måste veta exakt vilka kemikalier du har innan du blandar dem, måste du veta 
exakt vad dina mock-objekt kommer att göra innan du kör testet. Annars kan du få oväntade explosioner, eller 
i vårt fall, oväntade testresultat.

## Testa när allt går bra - Det lyckliga fallet

Vi börjar med att skriva en test för när allt går bra, det så kallade "happy path". Det är som att först 
kontrollera att en bil kan köra framåt på en rak väg innan vi testar vad som händer när bromsarna går sönder 
eller det regnar.

### Förberedande av det förväntade lånet

Vi behöver skapa ett förväntat lån. Varför? Tänk på det såhär: när vi anropar `loanRepository.save()` i riktig kod 
med en riktig databas, skulle den spara lånet i databasen och returnera det sparade lånet med ett genererat ID 
från databasen. Men eftersom vi använder en Mock, måste vi "programmera" eller "definiera" vad mock:en ska 
returnera när `save()` anropas.

Det är som att träna en skådespelare. Vi säger: "När regissören ropar ditt namn, säg denna replik". På samma sätt 
säger vi åt våra mock-objekt: "När någon frågar efter användare med ID 1, returnera vår testanvändare".

### Act-fasen: Vad händer bakom kulisserna

Under vår ACT-del testar vi den metoden vi ville testa:
```java
LoanDTO result = loanService.createLoan(createLoanDTO);
```

Detta händer när denna enda rad kod körs, som en dominoeffekt:

1. `loanService.createLoan()` anropas med vårt test-DTO
2. Metoden anropar `userRepository.findById(1L)` - vår mock returnerar testUser
3. Metoden anropar `bookRepository.findById(1L)` - vår mock returnerar testBook
4. Metoden kontrollerar att boken har tillgängliga exemplar (vårt testBook har 5 exemplar)
5. Metoden minskar tillgängliga exemplar med 1
6. Metoden skapar ett nytt Loan-objekt
7. Metoden anropar `loanRepository.save()` - vår mock returnerar expectedLoan
8. Metoden konverterar Loan till LoanDTO och returnerar det

Varje steg måste fungera perfekt för att kedjan ska hålla ihop.

### Assert-fasen: Kontrollera varje detalj

Under Assert-delen kontrollerar vi alla delar av metoden och lånet så att resultatet blir som vi ville. 
Vi kontrollerar inte bara att något kom tillbaka, utan att allt är exakt rätt - som en kvalitetskontroll 
som mäter varje del av en bil innan den lämnar fabriken.

Efter pjäsen vill vi också kontrollera att skådespelarna faktiskt sa sina repliker när de skulle. 
`verify()` kontrollerar att våra mock-objekt blev anropade på rätt sätt vid rätt tidpunkter. 
Det är som att ha en inspelning av hela föreställningen som vi kan spola igenom för 
att säkerställa att allt gick enligt manuscript.

## Testa när allt går fel - Viktiga felscenarier

Nu när vi skapat en test för det lyckliga scenariot behöver vi tänka på alla scenarier där det kan gå fel. 
I verkligheten går saker fel hela tiden, och vårt system måste hantera detta elegant. Tänk på det som att testa 
en bil under olika väderförhållanden - inte bara på soliga dagar utan också i regn, snö och storm.

De viktigaste felscenariona vi måste testa är:

1. **Någon försöker låna med user-id som inte finns** - Som att någon försöker checka in på ett hotell med ett falskt körkort
2. **Någon vill låna en bok som inte finns eller existerar** - Som att någon försöker låna en bok som biblioteket aldrig har ägt
3. **Någon försöker låna en bok som redan är slut hos oss** - Som att försöka köpa den sista mjölken när någon annan redan tagit den
4. **Boken som ska lånas ut har bara ett exemplar kvar** - Det kritiska gränsfallet

Tänk på att vid alla dessa scenarier måste en Exception kastas! 
Det är systemets sätt att säga "Stopp! Något är fel här!" istället för att bara fortsätta och skapa kaos.

### Varför never() är så viktigt

I dessa feltest-metoder använder vi oss av `never()` för att säkerställa att systemet slutar att fungera omedelbart 
när ett fel upptäcks.

Till exempel: `verify(bookRepository, never()).findById(any());`

Det är som att kontrollera att en säkerhetsvakt verkligen slutar leta efter fler problem när den första alarmsirenen går. 
Om användaren inte finns ska systemet inte fortsätta och leta efter böcker - det skulle vara slöseri med resurser och 
potentiellt farligt.

## Gränsfallstestning - Där buggar gömmer sig

Ett av de mest kritiska testen är när "boken som ska lånas ut har bara ett exemplar kvar". 
Det här är som att testa vad som händer när du häller ut exakt sista droppen mjölk ur paketet. 
Övergången från "har mjölk" till "är slut" måste fungera perfekt, annars kan systemet tro att det fortfarande 
finns mjölk när det faktiskt är slut.

Gränsfallstestning fångar upp flera kategorier av buggar som endast uppträder i specifika situationer:

**Off-by-one-errors:** Kod som kontrollerar `availableCopies > 1` istället för `availableCopies > 0` skulle misslyckas här.

**Aritmetikfel:** Kod som glömmer att minska `availableCopies` eller minskar det fel antal skulle upptäckas här.

**Tillståndshantering:** Detta test säkerställer att objektet lämnas i rätt tillstånd efter operationen.

Det är som att testa en bil vid exakt den temperatur då regn övergår till snö - många system fungerar 
bra i regn och bra i snö, men fallerar just vid övergången.

## Vad händer när tester misslyckas?

När tester blir röda och misslyckas är det inte dåligt - det är precis vad de ska göra! Tester är som vakthundar 
som skäller när något är fel. De hjälper oss hitta problem innan riktiga användare stöter på dem.

När ett test misslyckas berättar det för oss exakt vad som gick fel, var det gick fel, och vad som förväntades istället. 
Det är som att ha en detektiv som inte bara säger "något är fel" utan också "här är exakt vad som hände, här är bevisen, 
och här är vad som skulle ha hänt istället".

Varje rött test är en möjlighet att lära sig något nytt om din kod och göra den bättre. Det är som att ha en personlig 
tränare som pekar ut exakt vilka muskler som behöver tränas mer.

## Varför ordningen av tester spelar roll

Vi testar alltid det lyckliga fallet först, sedan feltesterna. Varför? Det är som att först kontrollera att en bil 
kan köra framåt på en rak väg innan du testar vad som händer när bromsarna går sönder. Du vill vara säker på att 
grundfunktionaliteten fungerar innan du börjar testa extremfall.

Om det lyckliga fallet inte fungerar finns det ingen anledning att testa felfall - du vet redan att något 
grundläggande är trasigt. Men om det lyckliga fallet fungerar och feltesten misslyckas, vet du att problemet ligger 
specifikt i felhanteringen.

## Sammanfattning: Bygga förtroende genom testning

När alla våra tester är gröna vet vi att vårt bibliotekssystem hanterar både vanliga situationer och ovanliga 
problem på rätt sätt. Det är som att ha en kvalitetskontroll som kollar varje del av systemet innan 
det når våra användare.

Tillsammans bygger dessa tester upp en säkerhetsmur runt vår kod. Varje test har sin specifika roll:
- Det lyckliga testet säkerställer att grundfunktionaliteten fungerar
- Feltesterna säkerställer att systemet hanterar problem elegant
- Gränsfallstesterna säkerställer att systemet fungerar även i ovanliga situationer

Med denna testsvit kan vi vara säkra på att vårt bibliotekssystem fungerar korrekt under alla omständigheter. 
Vi har förvandlat osäkerhet till förtroende, och det är testningens verkliga magi.