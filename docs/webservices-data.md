# Web Services Data Package Documentation

This document provides an overview of the `.data` package in the CARP Web Services project. The `.data` package is responsible for processing and invoking CARP Core data in the application. It includes repositories for accessing data from the database and services for processing data.

## Classes

### `DataStreamSequence`

This class represents a sequence of data stream points. It includes properties for the data stream ID, first sequence ID, last sequence ID, and a snapshot of the data stream. For more details, refer to the source code [here](../src/main/kotlin/dk/cachet/carp/webservices/data/domain/DataStreamSequence.kt).

### `CawsMutableDataStreamBatchWrapper`

This class is a wrapper for the `DataStreamBatch` and implements the `Sequence<DataStreamPoint<*>>` and `DataStreamBatch` interfaces. It provides methods for appending sequences and batches to the `DataStreamBatch`. For more details, refer to the source code [here](../src/main/kotlin/dk/cachet/carp/webservices/data/service/impl/CawsMutableDataStreamBatchWrapper.kt).

### `DataStreamId`

This class represents a unique identifier for a data stream. It includes properties for the study deployment ID, device role name, name, and namespace. For more details, refer to the source code [here](../src/main/kotlin/dk/cachet/carp/webservices/data/domain/DataStreamId.kt).

### `DataStreamSnapshot`

This class represents a snapshot of a data stream. It includes properties for measurements, trigger IDs, and a sync point. For more details, refer to the source code [here](../src/main/kotlin/dk/cachet/carp/webservices/data/domain/DataStreamSnapshot.kt).

## Services

### `CoreDataStreamService`

The `CoreDataStreamService` is a core component of the CARP Web Services data package. It is responsible for managing data streams within the application.

The `CoreDataStreamService` class implements the `DataStreamService` interface, which defines the contract for managing data streams. This includes methods for appending data to data streams, closing data streams, retrieving data from data streams, and processing of zip file before appending to data streams.

The `CoreDataStreamService` class relies on several repositories for data access, including `DataStreamConfigurationRepository`, `DataStreamIdRepository`, and `DataStreamSequenceRepository`. These repositories provide methods for interacting with the underlying database.

One of the key methods in this class is `appendToDataStreams()`, which is used to append a batch of data point sequences to corresponding data streams in a study deployment. This method performs several checks to ensure the validity of the data and throws exceptions if any issues are found.

Another important method is `getDataStream()`, which retrieves all data points in a data stream that fall within a specified range. If no data is available for the specified range, an empty `DataStreamBatch` is returned.

The `CoreDataStreamService` class also provides methods for managing the lifecycle of data streams, such as `openDataStreams()` and `closeDataStreams()`. These methods are used to start accepting data for a study deployment and to stop accepting incoming data, respectively.

For more details, refer to the source code [here](../src/main/kotlin/dk/cachet/carp/webservices/data/service/core/CoreDataStreamService.kt).

## Design

The `DataStreamService` interface is designed to be implemented by a concrete class that provides the necessary functionality for managing data streams. The `CoreDataStreamService` class is an example of such an implementation, which handles the core logic for managing data streams by invocation and call of services within the application.

### `Work flow & work arounds`

Preconditions and sequential checks for the core object `MutableDataStreamBatch` is unstable. It's on point to rely on frontend to produce the correct data with the correct data type, which is `DataStreamServiceRequest` within Core infrastructure. The `DataStreamBatch` is a core object that is used to store and manage data streams in the application. It provides methods for appending data point sequences to data streams, retrieving data from data streams, and managing the lifecycle of data streams.

![Subsystem decomposition](https://imgur.com/4wJ82Ib.png)

### `Coroutine processing & data stream`

The `CawsDataStreamService` service is responsible for running suspend coroutines for processing data streams. It includes method for creating tempFile within the process of unzipping files by line, reading serializing to and `DataStreamServiceRequest` and invoking Core. This services works as coroutine producer of data for the `DataStreamController`.

### `CawsDataStreamService`

#### `Zip file processing`

The `processToZip` function is responsible for processing zip files before appending the data to data streams. It includes methods for extracting data from zip files, validating the data, and converting it into the appropriate format for storage in the database.
At this point in time, the `processToZip` is fully implemented and been tested by initialization of `DataStreamBatch` and `DataStreamSequence` objects and functionally tested by `generateRandomDataStreamServiceRequest`.