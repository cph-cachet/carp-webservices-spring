# Resources Section

This section is designed to manage and organize various types of content, including local files, structured documents, and collections. This section is divided into three subsections: **Files**, **Collections**, and **Documents**.

## 1. Files
Please refer to the [API documentation](https://dev.carp.dk/doc) for Endpoints.

**Purpose**: The Files subsection manages interactions with the local file system, enabling users to upload, access, and download files.

- **File Upload**: Users can upload files from their local system. The system supports various file types and ensures they are stored securely.
- **File Access**: Uploaded files can be accessed through the interface. Users have the option to view the list of files, and download them as needed.
- **File Download**: Users can download files to their local system. The system ensures the files are accessible and downloadable in their original format.

**Storage**: Files are stored locally on the server or a designated storage service and are managed separately from documents and collections.

---

## 2. Collections
Please refer to the [API documentation](https://dev.carp.dk/doc) for Endpoints.

**Purpose**: Collections serve as a virtual file system, similar to folders or directories, that organizes and provides paths to documents.

- **Collection Structure**: Collections can be thought of as folders that contain documents. They allow users to group related documents together in a logical structure, such as `collection1/document1`.
- **Hierarchical Organization**: Collections can be nested or organized hierarchically, allowing for complex organizational structures.
- **Configuration Role**: Collections primarily function as configurations for documents, defining the structure and path through which documents are accessed.

**Storage**: Collections are stored in the database, and they do not contain actual content but instead act as metadata that defines the structure of the documents.

---

## 3. Documents
Please refer to the [API documentation](https://dev.carp.dk/doc) for Endpoints.

**Purpose**: Documents are the actual content stored within collections, and they are managed in a structured JSON format.

- **JSON Format**: Documents are stored in JSON format, allowing for structured, easily manageable data.
- **Document Path**: Each document is associated with a collection and is accessed via a path, such as `collection1/document1`.
- **Database Storage**: Documents are stored in a database, ensuring secure and efficient retrieval.
- **Image Upload Support**: Documents can include images, but the image data itself is not stored within the document. Instead:
  - **Image Upload**: Images are uploaded to an S3 bucket (or similar cloud storage service).
  - **Image Linking**: A link to the uploaded image is stored in the JSON document, allowing the document to reference the image without embedding the actual data.

---

## Summary

- **Files**: Handles traditional file management (upload, access, download) with local file system interactions.
- **Collections**: Organizes documents in a virtual file system, functioning as directories or folders within the project.
- **Documents**: Stores structured data in JSON format, with support for image links stored externally in an S3 bucket.

This structure allows for a flexible and organized approach to managing resources, with clear distinctions between file system interactions, virtual document organization, and structured content storage.
