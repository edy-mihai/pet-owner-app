# Class diagram

This is the planned class diagram for the mid-term. Dashed arrows mean "refers to by id": the class holds only the id, not the object. The domain classes depend on neither Spring nor any storage technology. The console (`Menu`) calls `PetCare`, which calls the storage interfaces.

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
    class VetVisit {
        -long id
        -long petId
        -LocalDate visitDate
        -String reason
        -String notes
        -BigDecimal cost
        -Long carePlanId
    }
    class TodayItem {
        <<record>>
    }
    class PetHistory {
        <<record>>
    }

    class AccountDirectory {
        <<interface>>
    }
    class PetRegister {
        <<interface>>
    }
    class CarePlanBook {
        <<interface>>
    }
    class VisitLog {
        <<interface>>
    }
    class InMemoryAccountDirectory
    class InMemoryPetRegister
    class InMemoryCarePlanBook
    class InMemoryVisitLog

    class PetCare {
        +addPet()
        +removeAccount()
        +todayFor()
        +markGiven()
        +recordVisit()
        +historyOf()
    }
    class PetHistoryReport {
        +format(PetHistory) List~String~
    }
    class HistoryFile {
        +write(Path, List~String~)
        +readBack(Path) List~String~
    }
    class Menu
    class Input
    class ConsoleApp

    AccountDirectory <|.. InMemoryAccountDirectory
    PetRegister <|.. InMemoryPetRegister
    CarePlanBook <|.. InMemoryCarePlanBook
    VisitLog <|.. InMemoryVisitLog
    PetCare ..> AccountDirectory
    PetCare ..> PetRegister
    PetCare ..> CarePlanBook
    PetCare ..> VisitLog
    PetCare ..> TodayItem
    PetCare ..> PetHistory
    Menu ..> PetCare
    Menu ..> Input
    Menu ..> PetHistoryReport
    Menu ..> HistoryFile
    ConsoleApp ..> Menu
    Account --> Role
    Pet --> Species
    CarePlan --> PlanKind
    CarePlan ..> DueStatus
    Pet ..> Account : owner ids
    CarePlan ..> Pet : petId
    VetVisit ..> Pet : petId
    VetVisit ..> CarePlan : optional carePlanId
```

## Mapping to database tables

| Java class | Table(s) |
|---|---|
| `Account` | `ACCOUNT` |
| `Pet` (and its `owners` map) | `PET`, `OWNERSHIP` |
| `CarePlan` | `CARE_PLAN` |
| `VetVisit` | `VET_VISIT` |

`PLACE` has no Java class for now.
