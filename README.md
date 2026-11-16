# Kompiuterio komplektavimo sistema

Pirkėjas pasirenka tiekėją, surenka kompiuterį iš aštuonių dalių ir mato, kaip pasirinkimai keičia kainą bei biudžeto likutį. Pasirinkus visas dalis ir įvedus biudžetą, serveris apskaičiuoja galutinę kainą ir patikrina suderinamumą. Tiekėjas savo valdymo skydelyje gali sukurti vaizdo plokštės juodraštį, jį paskelbti arba pašalinti.

## Ko reikia

| Įrankis | Projekto arba patikros versija |
| --- | --- |
| JDK | 21 |
| Maven | 3.10.0, atsisiunčiamas per projekto `mvnw` |
| PostgreSQL ir `psql` | 17 |
| Node.js ir npm | 24 ir 11 |

Backend naudoja Spring Boot 4.1.2, frontend – React, TypeScript ir Vite. Toliau pateiktos komandos skirtos macOS arba Linux terminalui. Jas vykdykite iš projekto šakninio aplanko, kuriame yra `backend` ir `frontend`. Reikės veikiančio PostgreSQL serverio ir naudotojo, galinčio kurti lenteles pasirinktoje bazėje.

## Pradinis paruošimas

1. Jei pradedate nuo tuščios aplinkos, sukurkite bazę. Pavyzdyje naudojamas vietinis PostgreSQL naudotojas, kurio vardas sutampa su terminalo naudotoju.

   ```sh
   createdb kompiuteriu_sistema
   ```

2. Terminale nustatykite prisijungimo duomenis. Jei naudojate kitą bazę arba PostgreSQL naudotoją, pakeiskite atitinkamas reikšmes. Jei prisijungimui reikia slaptažodžio, įrašykite jį į `DB_PASSWORD`.

   ```sh
   export DB_URL='jdbc:postgresql://localhost:5432/kompiuteriu_sistema'
   export DB_USER="$(whoami)"
   export DB_PASSWORD=''
   export BALTIC_API_KEY="$(openssl rand -hex 32)"
   export NORDIC_API_KEY="$(openssl rand -hex 32)"
   ```

   Tai dviejų įmonių valdymo raktai. „Baltic PC“ priskirtas ID 1, „Nordic Systems“ – ID 2. Esamoje aplinkoje naudokite jau nustatytus raktus. Naujame terminale aplinkos kintamuosius reikia nustatyti iš naujo. Tikras reikšmes laikykite vietinėje konfigūracijoje už Git ribų.

3. Įdiekite frontend priklausomybes.

   ```sh
   npm --prefix frontend ci
   ```

## Paleidimas viena komanda

Tame pačiame terminale iš projekto šaknies paleiskite:

```sh
bash -c 'cd frontend || exit 1; node node_modules/vite/bin/vite.js --port 5173 --strictPort & ui=$!; trap "kill $ui 2>/dev/null || true" EXIT; cd ../backend && ./mvnw spring-boot:run'
```

Palaukite, kol backend terminale pasirodys `Started BackendApplication`. Atidarykite [konfigūratorių](http://localhost:5173). Frontend persiunčia `/api` užklausas į backend, kuris veikia 8081 prievadu. Stabdykite su **Ctrl+C**.

Pirmą kartą Hibernate sukuria lenteles. Patikrinti, kokius tiekėjus mato programa, galima atidarius [įmonių sąrašą](http://localhost:8081/api/companies). Jei naudojate jau užpildytą projekto bazę, pereikite prie demonstravimo.

## Pavyzdiniai duomenys naujai bazei

Šie veiksmai skirti vienkartiniam naujos demonstracinės bazės užpildymui. Palikite programą veikti. Antrame terminale naudokite tuos pačius DB prisijungimo duomenis ir **tas pačias API raktų reikšmes**, su kuriomis paleidote backend.

1. Įmones sukurkite po pirmo backend paleidimo, kol `companies` lentelė ir jos ID seka dar tuščios.

   ```sh
   PGPASSWORD="$DB_PASSWORD" psql "${DB_URL#jdbc:}" -U "$DB_USER" -v ON_ERROR_STOP=1 -c "INSERT INTO companies (name) VALUES ('Baltic PC'), ('Nordic Systems') RETURNING id, name;"
   ```

   Patikrinkite, ar „Baltic PC“ gavo ID 1, o „Nordic Systems“ – ID 2.

2. Įkelkite vieną suderinamą „Baltic PC“ komplektą ir kitos įmonės vaizdo plokštę. Kainos ir techniniai duomenys čia naudojami demonstravimui.

   ```sh
   add_part() {
     curl -fsS "http://localhost:8081/api/companies/$1/$3" \
       -H "X-API-Key: $2" -H 'Content-Type: application/json' -d "$4"
   }

   add_part 1 "$BALTIC_API_KEY" motherboards '{"name":"MSI PRO B760M-A DDR4","price":150,"socket":"LGA1700","memoryType":"DDR4","supportedStorageTypes":["M2_2280_PCIE"]}'
   add_part 1 "$BALTIC_API_KEY" processors '{"name":"Intel Core i5-12400F","price":160,"socket":"LGA1700","powerConsumptionW":100}'
   add_part 1 "$BALTIC_API_KEY" memories '{"name":"Kingston FURY Beast","price":50,"memoryType":"DDR4","capacityGb":16}'
   add_part 1 "$BALTIC_API_KEY" storages '{"name":"WD Blue SN580","price":70,"interfaceType":"M2_2280_PCIE","capacityGb":1000}'
   add_part 1 "$BALTIC_API_KEY" cases '{"name":"Corsair 4000D Airflow","price":90,"maxGpuLengthMm":360,"maxCoolerHeightMm":170}'
   add_part 1 "$BALTIC_API_KEY" coolers '{"name":"DeepCool AK400","price":35,"supportedSockets":["LGA1700"],"heightMm":155}'
   add_part 1 "$BALTIC_API_KEY" power-supplies '{"name":"Corsair RM650","price":75,"wattage":650}'
   add_part 1 "$BALTIC_API_KEY" graphics-cards '{"name":"GeForce RTX 3060","price":250,"lengthMm":240,"powerConsumptionW":170}'
   add_part 2 "$NORDIC_API_KEY" graphics-cards '{"name":"Radeon RX 6600","price":200,"lengthMm":220,"powerConsumptionW":132}'
   ```

3. Abi vaizdo plokštės sukuriamos kaip juodraščiai. Sąsajoje pasirinkite „Baltic PC“, atverkite tiekėjo valdymą, įveskite šios įmonės raktą ir paskelbkite RTX 3060. „Nordic Systems“ juodraštį galite palikti izoliavimo demonstracijai.

## Demonstravimas

1. Konfigūratoriuje pasirinkite „Baltic PC“ ir įveskite 1 000 € biudžetą. Pasirinkite visas aštuonias aukščiau sukurtas dalis. Jų suma yra **880 €**, lieka **120 €**, suderinamumo problemų sąrašas tuščias.
2. Sumažinkite biudžetą iki **800 €**. Su tuo pačiu komplektu sąsaja parodo **80 €** viršijimą. Viena iš serverio taisyklių yra vaizdo plokštės ilgio palyginimas su korpuso riba. Lygūs matmenys leidžiami.
3. Patikrinkite įmonių izoliavimą. Viešame kataloge pirkėjas gali rinktis abiejų įmonių paskelbtas dalis. Privačiam tiekėjo katalogui reikia tos įmonės rakto. Abi žemiau pateiktos užklausos skaito tą patį „Nordic Systems“ katalogą. Pirmoji turi grąžinti **403**, antroji – **200** ir šios įmonės juodraštį.

   ```sh
   curl -i -H "X-API-Key: $BALTIC_API_KEY" http://localhost:8081/api/companies/2/management/graphics-cards
   curl -i -H "X-API-Key: $NORDIC_API_KEY" http://localhost:8081/api/companies/2/management/graphics-cards
   ```

## Testai ir kodo patikros

Integraciniai testai jungiasi prie PostgreSQL. Vieną kartą sukurkite atskirą testų bazę su tuo pačiu DB naudotoju. Komanda daro prielaidą, kad PostgreSQL veikia lokaliai 5432 prievadu.

```sh
PGPASSWORD="$DB_PASSWORD" createdb -h localhost -U "$DB_USER" kompiuteriu_sistema_test
```

Iš projekto šaknies paleiskite:

```sh
(cd backend && DB_URL=jdbc:postgresql://localhost:5432/kompiuteriu_sistema_test ./mvnw test)
(cd backend && ./mvnw spotless:check)
npm --prefix frontend run lint
npm --prefix frontend run build
```

`BuildEvaluationServiceTest` tikrina kainą ir biudžeto rezultatą, `CompatibilityServiceTest` – komponentų suderinamumo taisykles. `CompanyIsolationTest` tikrina duomenų atskyrimą ir juodraščio matomumą, o `CompanyApiSecurityTest` – prieigą per HTTP.

## Kodo struktūra

- **backend** – `controller` priima HTTP užklausas, `service` vykdo verslo taisykles, `repository` skaito ir saugo duomenis, `model` aprašo įmones ir komponentus. `security` susieja API raktą su įmone ir tikrina prieigą.
- **frontend** – `src/features/configurator` pateikia komponentų pasirinkimą, biudžetą ir suderinamumo rezultatą. `src/features/supplier` pateikia tiekėjo vaizdo plokščių valdymą.
