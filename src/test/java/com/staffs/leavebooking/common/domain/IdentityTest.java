package com.staffs.leavebooking.common.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Identity Value Object")
class IdentityTest {

    @Nested
    @DisplayName("Construction validation")
    class ConstructionValidation {

        @Test
        @DisplayName("Should reject null id")
        void shouldRejectNullId() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Identity<>(null));
            assertEquals(Identity.IDENTITY_CANNOT_BE_NULL, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject blank id")
        void shouldRejectBlankId() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Identity<>("   "));
            assertEquals(Identity.IDENTITY_CANNOT_BE_NULL, ex.getMessage());
        }

        @Test
        @DisplayName("Should reject empty string id")
        void shouldRejectEmptyStringId() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Identity<>(""));
            assertEquals(Identity.IDENTITY_CANNOT_BE_NULL, ex.getMessage());
        }

        @Test
        @DisplayName("Should accept non-UUID format string (Firebase UID)")
        void shouldAcceptFirebaseUid() {
            String firebaseUid = "D806yr3XxhgN6sbXZRLHDyH12345";

            Identity<?> identity = new Identity<>(firebaseUid);

            assertEquals(firebaseUid, identity.id());
        }

        @Test
        @DisplayName("Should accept any non-blank string as identity")
        void shouldAcceptAnyNonBlankString() {
            Identity<?> identity = new Identity<>("some-custom-id-format");

            assertEquals("some-custom-id-format", identity.id());
        }

        @Test
        @DisplayName("Should accept valid UUID format")
        void shouldAcceptValidUuid() {
            String validUuid = "550e8400-e29b-41d4-a716-446655440000";

            Identity<?> identity = new Identity<>(validUuid);

            assertEquals(validUuid, identity.id());
        }
    }

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("of() should create identity from valid UUID string")
        void ofShouldCreateFromValidString() {
            String uuid = UUID.randomUUID().toString();

            Identity<?> identity = Identity.of(uuid);

            assertEquals(uuid, identity.id());
        }

        @Test
        @DisplayName("generateId() should produce valid UUID identity")
        void generateIdShouldProduceValidUuid() {
            Identity<?> identity = Identity.generateId();

            assertNotNull(identity);
            assertNotNull(identity.id());
            assertDoesNotThrow(() -> UUID.fromString(identity.id()));
        }

        @Test
        @DisplayName("generateId() should produce unique identities")
        void generateIdShouldProduceUniqueIds() {
            Identity<?> id1 = Identity.generateId();
            Identity<?> id2 = Identity.generateId();

            assertNotEquals(id1, id2);
        }
    }

    @Nested
    @DisplayName("Equality semantics")
    class EqualitySemantics {

        @Test
        @DisplayName("Two identities with same UUID should be equal")
        void sameUuidShouldBeEqual() {
            String uuid = UUID.randomUUID().toString();

            Identity<?> id1 = Identity.of(uuid);
            Identity<?> id2 = Identity.of(uuid);

            assertEquals(id1, id2);
            assertEquals(id1.hashCode(), id2.hashCode());
        }

        @Test
        @DisplayName("Two identities with different UUIDs should not be equal")
        void differentUuidsShouldNotBeEqual() {
            Identity<?> id1 = Identity.generateId();
            Identity<?> id2 = Identity.generateId();

            assertNotEquals(id1, id2);
        }
    }
}
