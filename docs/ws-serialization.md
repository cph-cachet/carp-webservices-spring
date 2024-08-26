# Serialization Strategy

Our project utilizes a mix of serialization strategies tailored to specific components and requirements. The serialization frameworks employed include `kotlinx.serialization`, Java serialization, and Jackson. Each is used in different contexts to efficiently manage data serialization and deserialization across the application.

## Serialization Frameworks Overview

### 1. Kotlinx.Serialization
Please refer to the [CARP serialization documentation](https://github.com/cph-cachet/carp.core-kotlin/blob/develop/docs/serialization.md) for detailed information.
**Purpose**: `kotlinx.serialization` is primarily used for carp.core content serialization . It is well-suited for Kotlin data classes and provides a straightforward, type-safe way to handle JSON serialization and deserialization.

- **Usage Context**:
  - Core endpoints: all service-request endpoints
  - Core data classes and objects: Snapshot, StudyStatus, etc.

### 2. Jackson

**Purpose**: Jackson is utilized in several areas, including web service domains and database operations, it is (still) the default serialization strategy for Spring http requests.

- **Usage Context**:
  - **Web Service Domains**: Jackson is used for serializing and deserializing domain objects in our web services, ensuring compatibility with REST APIs and Spring Framework. This includes DataStreams, Documents, Collections, etc.
  - **Database JSONB Objects**: Jackson is also employed to handle JSONB (Binary JSON) objects stored in the database, facilitating smooth database interactions.

### 3. Java Serialization

**Purpose**: Java serialization is used in legacy parts of the application, particularly for older components where backwards compatibility is essential.

- **Usage Context**:
  - Legacy systems and components
  - Specific web service domains where Java serialization has been historically used

## Strategy Breakdown

### Core Content

- **Serialization Framework**: `kotlinx.serialization`
- **Description**: We use `kotlinx.serialization` to handle the serialization of core content, particularly for data classes and objects within the Kotlin codebase. This ensures type safety and a clean integration with Kotlin's features.

### Web Services Domains

- **Serialization Framework**: Jackson / Java Serialization, we are in the process of transitioning to `kotlinx.serialization`
- **Description**: For web service domains, particularly those interfacing with REST APIs or Spring services, we use Jackson to serialize and deserialize domain objects. In some legacy web services, Java serialization is used to maintain compatibility with existing systems.

### Database JSONB Objects

- **Serialization Framework**: Jackson
- **Description**: Jackson is employed for handling JSONB objects in the database. This allows us to efficiently store and retrieve JSON data from the database while leveraging Jackson's powerful JSON processing capabilities.

## Summary

- **Kotlinx.Serialization**: Used for core content and Kotlin data classes.
- **Jackson**: Utilized for web services (REST API domains) and database JSONB objects.
- **Java Serialization**: Applied in legacy systems and specific web service domains where historical use dictates its continued use.
