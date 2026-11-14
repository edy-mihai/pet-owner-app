## Business Rules

**_R1:_** Every account has exactly one role, owner or admin, and a unique email.

**_R2:_** An admin account never owns a pet.

**_R3:_** Every pet has at least one owner. A pet is created together with its first ownership.

**_R4:_** The same account can be linked to the same pet only once. Co-owners are equal, with no main owner.

**_R5:_** When an account is deleted, its pets that have other owners stay. A pet whose last owner is deleted is deleted with its care plans and visits.

**_R6:_** A care plan has a positive interval or none (a one-off). If it has an end date, the end date is not before the start date. A one-off has no end date.

**_R7:_** A care plan’s kind is one of five categories: medication, vaccine, checkup, parasite treatment, other.

**_R8:_** A plan can be marked as given only once per day, and never for a future date.

**_R9:_** Completing a scheduled visit records the visit, links it to the plan, and sets the plan’s last-given date to the visit date, all in one transaction. A one-off plan is then done.

**_R10:_** A visit may point to a place only if that place is a vet.

**_R11:_** A visit’s care plan must belong to the same pet as the visit.

**_R12:_** A visit’s cost, if given, is not negative.
