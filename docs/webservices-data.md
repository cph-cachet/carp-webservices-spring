# Web Services Data Package Documentation

This document provides an overview of the `.data` package in the CARP Web Services project. The `.data` package is responsible for processing and invoking CARP CORE data in the application. It includes repositories for accessing data from the database and services for processing data.

## Classes

### `DataStreamSequence`

This class represents a sequence of data stream points. It includes properties for the data stream ID, first sequence ID, last sequence ID, and a snapshot of the data stream. For more details, refer to the source code [here](src/main/kotlin/dk/cachet/carp/webservices/data/domain/DataStreamSequence.kt).

### `CawsMutableDataStreamBatchWrapper`

This class is a wrapper for the `DataStreamBatch` and implements the `Sequence<DataStreamPoint<*>>` and `DataStreamBatch` interfaces. It provides methods for appending sequences and batches to the `DataStreamBatch`. For more details, refer to the source code [here](src/main/kotlin/dk/cachet/carp/webservices/data/service/impl/CawsMutableDataStreamBatchWrapper.kt).

## Services

### `CoreDataStreamService`

This class implements the `DataStreamService` interface and provides methods for appending data to data streams, closing data streams, retrieving data from data streams, opening data streams, and removing data streams. For more details, refer to the source code [here](src/main/kotlin/dk/cachet/carp/webservices/data/service/core/CoreDataStreamService.kt).

### `DataStreamServiceWrapper`

This class is a wrapper for the `DataStreamService` and implements the `DataStreamService` and `ResourceExporter<DataStreamSequence>` interfaces. It provides methods for retrieving the latest update timestamp for a given deployment, processing a zip file and invoking the appropriate service method, and extracting a `DataStreamServiceRequest` from a zipped file. For more details, refer to the source code [here](src/main/kotlin/dk/cachet/carp/webservices/data/service/impl/DataStreamServiceWrapper.kt).

## Interfaces

### `DataStreamService`