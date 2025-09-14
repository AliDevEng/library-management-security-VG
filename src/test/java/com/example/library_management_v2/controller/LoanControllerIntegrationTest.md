# Så här bygger du integrationstester för Spring Boot

## Vad är ett integrationstest?

Ett integrationstest skiljer sig från ett enhetstest genom att det testar hela systemet tillsammans. 
Istället för att isolera en enskild metod testar vi hela flödet från HTTP-anrop till databas. 
Tänk på det som skillnaden mellan att testa en motors delar separat kontra att sätta motorn i en bil och köra den.

## Skillnaden mellan unit-tester och integrationstester

Unit-tester är snabba och testar bara en liten bit kod åt gången. De använder fejk-objekt så vi har full 
kontroll över vad som händer. Om något går fel vet vi exakt var problemet är.
Integrationstester är långsammare men mer realistiska. De använder riktiga databaser och riktiga HTTP-anrop, 
precis som en riktig användare skulle göra. Om något går fel måste vi leta lite mer för att hitta problemet, 
men vi vet att hela systemet fungerar tillsammans.
Båda typerna behövs. Unit-tester hjälper oss när vi skriver kod, och integrationstester ger oss trygghet att 
allt fungerar för våra användare.

## Grundläggande setup

För att skapa ett integrationstest börjar vi med att sätta upp rätt miljö. 
Vi vill använda en testdatabas som är separerad från vår produktionsdatabas, 
så vi skapar en H2 in-memory databas som existerar bara under testets körning.

Skapa först en testkonfiguration som hanterar databasen:
```java
@TestConfiguration
@Profile("test")
public class TestDatabaseConfig {

    @Bean
    @Primary // Denna databas har högsta prioritet under tester
    public DataSource testDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
```
## Byggandet av själva testet

Integrationstestet använder MockMvc för att simulera HTTP-anrop. Detta låter oss testa våra controllers 
utan att starta en riktig webbserver. Testet följer arrange-act-assert mönstret där vi förbereder testdata, 
utför operationen och verifierar resultatet.
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDatabaseConfig.class)
@Transactional
public class LoanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Repositories för att skapa testdata
    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    public void setUp() {
        // Skapa testdata i databasen före varje test
        // Detta säkerställer att varje test börjar med rent tillstånd
    }
    
    @Test
    public void testCreateLoan_Success() throws Exception {
        // Förbered testdata
        CreateLoanDTO loanRequest = new CreateLoanDTO();
        loanRequest.setUserId(testUser.getId());
        loanRequest.setBookId(testBook.getId());
        
        // Gör HTTP-anropet
        mockMvc.perform(post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()));
    }
}
```

## Viktiga annotationer och vad de gör

När vi skriver integrationstester använder vi fyra viktiga annotations. 
Varje annotation säger åt Spring att göra något specifikt:

@SpringBootTest säger åt Spring att starta upp hela applikationen. Det är som att starta en riktig bil istället 
för att bara testa motorn på en arbetsbänk. Allt kommer igång - alla controllers, services, repositories och databaser.

@AutoConfigureMockMvc ger oss ett verktyg som kan simulera HTTP-anrop utan att starta en riktig webbserver. 
Det är som att ha en robot som kan klicka på knappar på din webbsida, fast allt händer i datorns minne.

@ActiveProfiles("test") säger åt Spring att använda testinställningar istället för vanliga inställningar. 
Du kan ha en särskild fil för testning som använder andra databaser eller inställningar.

@Transactional är den viktigaste för testning. Den gör så att allt som händer i testet rullas tillbaka efteråt. 
Som att ha en ångra-knapp som gör att databasen blir som innan testet körde. På så sätt påverkar inte testerna varandra.

## Problemet jag stötte på under testet

Under utvecklingen mötte vi en klassisk konfigurationskonflikt. Vår produktionskonfiguration 
för SQLite kolliderade med testkonfigurationen för H2-databasen. Spring Boot blev förvirrad 
eftersom den fick två olika instruktioner om vilken databas som skulle användas.
Felet visade sig som "Could not load JDBC driver class [org.h2.Driver]" trots att vi hade 
konfigurerat H2 för testerna. Roten till problemet var att vår SQLiteConfig-klass försökte 
skapa en DataSource även under tester.

## Lösningen

Vi löste detta genom att använda Spring Boot:s profilsystem för att separera konfigurationerna. 
Vi lade till @Profile("!test") på SQLiteConfig-klassen, vilket betyder att den bara aktiveras när 
test-profilen inte körs.
Dessutom skapade vi en explicit testkonfiguration med @Primary-annotation, vilket gav testdatabasen 
högsta prioritet. Detta eliminerade alla tvetydigheter om vilken databaskonfiguration som skulle användas 
under tester.
Slutresultatet blev en ren separation mellan produktions- och testmiljöerna, där varje miljö har sin 
egen databaskonfiguration utan konflikter.