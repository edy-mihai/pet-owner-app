# Class diagram

This is the class diagram of the mid-term code (console, in-memory storage). Dashed arrows mean "uses" or "refers to by id": a class that refers to another by id holds only the id, not the object. The domain classes (`account`, `pet`, `careplan`) depend on neither Spring nor any storage technology. The console (`Menu`) calls `PetCare`, which calls the storage interfaces. For readability the diagram shows the main methods only, and it leaves out the `Style` arrows (used by `Menu` and `Input`) and most exception arrows.

```mermaid
classDiagram
    direction LR

    class Role {
        <<enumeration>>
        OWNER
        ADMIN
    }
    class Species {
        <<enumeration>>
        DOG
        CAT
        RABBIT
        BIRD
        OTHER
    }
    class PlanKind {
        <<enumeration>>
        MEDICATION
        VACCINE
        CHECKUP
        PARASITE_TREATMENT
        OTHER
    }
    class DueStatus {
        <<enumeration>>
        OVERDUE
        DUE_TODAY
        GIVEN_TODAY
        NOT_DUE
        NOT_STARTED
        ENDED
        DONE
    }

    class Account {
        -long id
        -String name
        -String email
        -Role role
    }
    class Pet {
        -long id
        -String name
        -Species species
        -LocalDate birthDate
        -String notes
        -Map~Long,LocalDate~ owners
        +addOwner(long accountId, LocalDate since) boolean
        +removeOwner(long accountId) boolean
        +isOwnedBy(long accountId) boolean
    }
    class CarePlan {
        -long id
        -long petId
        -String title
        -PlanKind kind
        -LocalDate startDate
        -Integer intervalDays
        -LocalDate endDate
        -LocalDate lastGivenDate
        +nextDue() Optional~LocalDate~
        +statusOn(LocalDate today) DueStatus
        +markGiven(LocalDate givenOn, LocalDate today)
    }
    class TodayItem {
        <<record>>
        String petName
        long planId
        String planTitle
        PlanKind kind
        DueStatus status
        Optional~LocalDate~ nextDue
        LocalDate lastGiven
    }
    class PetHistory {
        <<record>>
        Pet pet
        List~String~ ownerNames
        List~CarePlan~ plans
        LocalDate exportedOn
    }

    class AccountDirectory {
        <<interface>>
        +save(Account) Account
        +findById(long) Optional~Account~
        +list() List~Account~
        +delete(long)
    }
    class PetRegister {
        <<interface>>
        +save(Pet) Pet
        +findById(long) Optional~Pet~
        +list() List~Pet~
        +findByOwner(long accountId) List~Pet~
        +delete(long)
    }
    class CarePlanBook {
        <<interface>>
        +save(CarePlan) CarePlan
        +findById(long) Optional~CarePlan~
        +findByPetId(long petId) List~CarePlan~
        +deleteByPetId(long petId)
    }
    class InMemoryAccountDirectory {
        -Map~Long,Account~ accounts
    }
    class InMemoryPetRegister {
        -Map~Long,Pet~ pets
    }
    class InMemoryCarePlanBook {
        -Map~Long,CarePlan~ plans
    }

    class PetCare {
        +addAccount(String, String, Role) Account
        +addPet(long ownerId, String, Species, LocalDate, String, LocalDate today) Pet
        +removeAccount(long accountId)
        +deletePet(long petId)
        +addOwner(long petId, long accountId, LocalDate today) boolean
        +removeOwner(long petId, long accountId) boolean
        +addCarePlan(long petId, String, PlanKind, LocalDate, Integer, LocalDate) CarePlan
        +todayFor(long accountId, LocalDate today) List~TodayItem~
        +markGiven(long planId, LocalDate givenOn, LocalDate today)
        +historyOf(long petId, LocalDate today) PetHistory
    }
    class PetHistoryReport {
        +format(PetHistory) List~String~
    }
    class HistoryFile {
        +write(Path dir, String fileName, List~String~) Path
        +read(Path) List~String~
        +list(Path dir) List~Path~
        +fileName(String petName, long petId, LocalDate date)$ String
    }

    class ConsoleApp {
        +main(String[] args)$
    }
    class Options {
        <<record>>
        Path exportDir
        boolean demo
        boolean noColor
        boolean color
        +parse(String[] args)$ Options
        +colorsEnabled(boolean terminal, boolean noColorEnvSet) boolean
    }
    class DemoData {
        +load(PetCare, LocalDate today)$ Summary
    }
    class Style {
        +error(String) String
        +success(String) String
    }
    class Input {
        +readInt(String, int min, int max) int
        +readNonBlank(String) String
        +readDate(String) LocalDate
        +readChoice(String, Class~E~) E
    }
    class Menu {
        +run()
    }

    class RuntimeException {
        <<Java>>
    }
    class DuplicateEmailException
    class AdminCannotOwnPetException
    class CannotRemoveLastOwnerException
    class AlreadyGivenException
    class EndOfInputException

    AccountDirectory <|.. InMemoryAccountDirectory
    PetRegister <|.. InMemoryPetRegister
    CarePlanBook <|.. InMemoryCarePlanBook

    PetCare ..> AccountDirectory
    PetCare ..> PetRegister
    PetCare ..> CarePlanBook
    PetCare ..> TodayItem
    PetCare ..> PetHistory

    Menu ..> PetCare
    Menu ..> Input
    Menu ..> PetHistoryReport
    Menu ..> HistoryFile
    ConsoleApp ..> Options
    ConsoleApp ..> DemoData
    ConsoleApp ..> Menu
    DemoData ..> PetCare
    PetHistoryReport ..> PetHistory
    PetHistory --> Pet
    PetHistory --> CarePlan

    Account --> Role
    Pet --> Species
    CarePlan --> PlanKind
    CarePlan ..> DueStatus
    Pet ..> Account : owner ids
    CarePlan ..> Pet : petId

    RuntimeException <|-- DuplicateEmailException
    RuntimeException <|-- AdminCannotOwnPetException
    RuntimeException <|-- CannotRemoveLastOwnerException
    RuntimeException <|-- AlreadyGivenException
    RuntimeException <|-- EndOfInputException
    InMemoryAccountDirectory ..> DuplicateEmailException : throws
    PetCare ..> AdminCannotOwnPetException : throws
    Pet ..> CannotRemoveLastOwnerException : throws
    CarePlan ..> AlreadyGivenException : throws
    Input ..> EndOfInputException : throws
```

## Notes

- **Vet visits are not built in the mid-term.** They are in the ER diagram and in the business rules, but there is no `VetVisit`, `VisitLog` or visit code yet. `PetHistoryReport` builds the report one section per method, so a vet visits section can be added later.
- **Exceptions** are all unchecked (`RuntimeException`), one per rule, in the root package: `DuplicateEmailException` (same email twice), `AdminCannotOwnPetException` (an ADMIN account cannot own a pet), `CannotRemoveLastOwnerException` (a pet always keeps one owner), `AlreadyGivenException` (a one-off given twice, or a date not after the last one) and `EndOfInputException` (the console input ended).
- **Static methods** are marked with `$`: `HistoryFile.fileName`, `Options.parse`, `DemoData.load` and `ConsoleApp.main`.

## Java course requirements

- **Classes (more than 4), all about the application:** `Account`, `Pet`, `CarePlan`, `PetCare`, `PetHistoryReport`, `HistoryFile`, `Menu`, `Input`, `Options`, `DemoData`, `Style`, `ConsoleApp` and the three `InMemory` stores.
- **Interfaces (2 or more):** `AccountDirectory`, `PetRegister` and `CarePlanBook`. They are named after the domain, not `Repository` or `Manager`, and each has an in-memory implementation that could be replaced by a database one.
- **Collections used by the application:**
  - `Map<Long, LocalDate> owners` in `Pet` (a `HashMap`: owner account id to the date they became an owner).
  - `Map<Long, Account>`, `Map<Long, Pet>` and `Map<Long, CarePlan>` (`HashMap`s) in the three `InMemory` stores.
  - `List`s: the `ArrayList` of items built and sorted in `PetCare.todayFor`, `PetHistory.ownerNames` and `PetHistory.plans`, the lines returned by `PetHistoryReport.format`, and the files returned by `HistoryFile.list`.
- **File feature:** `HistoryFile` writes a pet's history to a text file and reads it back (the Pets menu: "Export history" and "View an exported history").

## Mapping to database tables

| Java class | Table(s) |
|---|---|
| `Account` | `ACCOUNT` |
| `Pet` (and its `owners` map) | `PET`, `OWNERSHIP` |
| `CarePlan` | `CARE_PLAN` |

`PLACE` and `VET_VISIT` have no Java class for now.
