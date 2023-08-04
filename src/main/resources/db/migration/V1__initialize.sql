--
-- PostgreSQL database dump
--

-- Dumped from database version 15.2 (Debian 15.2-1.pgdg110+1)
-- Dumped by pg_dump version 15.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: collections; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.collections (
    id integer NOT NULL,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255),
    document_id integer,
    name character varying(255) NOT NULL,
    study_id character varying(255) NOT NULL,
    study_deployment_id character varying(255)
);


ALTER TABLE public.collections OWNER TO admin;

--
-- Name: collections_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.collections_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.collections_id_seq OWNER TO admin;

--
-- Name: collections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.collections_id_seq OWNED BY public.collections.id;


--
-- Name: consent_documents; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.consent_documents (
    id integer NOT NULL,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255),
    data jsonb,
    deployment_id character varying(255)
);


ALTER TABLE public.consent_documents OWNER TO admin;

--
-- Name: consent_documents_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.consent_documents_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.consent_documents_id_seq OWNER TO admin;

--
-- Name: consent_documents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.consent_documents_id_seq OWNED BY public.consent_documents.id;


--
-- Name: data_points; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.data_points (
    id integer NOT NULL,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255),
    carp_body jsonb,
    carp_header jsonb,
    deployment_id character varying(255),
    storage_name character varying(255)
);


ALTER TABLE public.data_points OWNER TO admin;

--
-- Name: data_points_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.data_points_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_points_id_seq OWNER TO admin;

--
-- Name: data_points_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.data_points_id_seq OWNED BY public.data_points.id;


--
-- Name: data_stream_configurations; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.data_stream_configurations (
    study_deployment_id character varying(255) NOT NULL,
    closed boolean,
    config jsonb NOT NULL
);


ALTER TABLE public.data_stream_configurations OWNER TO admin;

--
-- Name: data_stream_ids; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.data_stream_ids (
    id integer NOT NULL,
    study_deployment_id character varying(255) NOT NULL,
    device_role_name character varying(255),
    name character varying(255),
    name_space character varying(255),
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255)
);


ALTER TABLE public.data_stream_ids OWNER TO admin;

--
-- Name: data_stream_ids_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.data_stream_ids_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_stream_ids_id_seq OWNER TO admin;

--
-- Name: data_stream_ids_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.data_stream_ids_id_seq OWNED BY public.data_stream_ids.id;


--
-- Name: data_stream_sequence; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.data_stream_sequence (
    id integer NOT NULL,
    data_stream_id integer,
    snapshot jsonb,
    first_sequence_id integer,
    last_sequence_id integer,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255)
);


ALTER TABLE public.data_stream_sequence OWNER TO admin;

--
-- Name: data_stream_sequence_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.data_stream_sequence_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.data_stream_sequence_id_seq OWNER TO admin;

--
-- Name: data_stream_sequence_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.data_stream_sequence_id_seq OWNED BY public.data_stream_sequence.id;


--
-- Name: deployments; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.deployments (
    id integer NOT NULL,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255),
    deployed_from_study_id character varying(255),
    snapshot jsonb
);


ALTER TABLE public.deployments OWNER TO admin;

--
-- Name: deployments_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.deployments_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.deployments_id_seq OWNER TO admin;

--
-- Name: deployments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.deployments_id_seq OWNED BY public.deployments.id;


--
-- Name: documents; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.documents (
    id integer NOT NULL,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255),
    collection_id integer NOT NULL,
    data jsonb,
    name character varying(255) NOT NULL
);


ALTER TABLE public.documents OWNER TO admin;

--
-- Name: documents_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.documents_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.documents_id_seq OWNER TO admin;

--
-- Name: documents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.documents_id_seq OWNED BY public.documents.id;


--
-- Name: files; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.files (
    id integer NOT NULL,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255),
    metadata jsonb,
    original_name character varying(255) NOT NULL,
    storage_name character varying(255) NOT NULL,
    study_id character varying(255) NOT NULL,
    study_deployment_id character varying(255)
);


ALTER TABLE public.files OWNER TO admin;

--
-- Name: files_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.files_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.files_id_seq OWNER TO admin;

--
-- Name: files_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.files_id_seq OWNED BY public.files.id;


--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO admin;

--
-- Name: participant_groups; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.participant_groups (
    id integer NOT NULL,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255),
    snapshot jsonb
);


ALTER TABLE public.participant_groups OWNER TO admin;

--
-- Name: participant_groups_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.participant_groups_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.participant_groups_id_seq OWNER TO admin;

--
-- Name: participant_groups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.participant_groups_id_seq OWNED BY public.participant_groups.id;


--
-- Name: protocols; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.protocols (
    id integer NOT NULL,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255),
    snapshot jsonb,
    version_tag character varying(255)
);


ALTER TABLE public.protocols OWNER TO admin;

--
-- Name: protocols_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.protocols_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.protocols_id_seq OWNER TO admin;

--
-- Name: protocols_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.protocols_id_seq OWNED BY public.protocols.id;


--
-- Name: recruitments; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.recruitments (
    id integer NOT NULL,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255),
    snapshot jsonb
);


ALTER TABLE public.recruitments OWNER TO admin;

--
-- Name: recruitments_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.recruitments_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.recruitments_id_seq OWNER TO admin;

--
-- Name: recruitments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.recruitments_id_seq OWNED BY public.recruitments.id;


--
-- Name: studies; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.studies (
    id integer NOT NULL,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255),
    snapshot jsonb
);


ALTER TABLE public.studies OWNER TO admin;

--
-- Name: studies_id_seq; Type: SEQUENCE; Schema: public; Owner: admin
--

CREATE SEQUENCE public.studies_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.studies_id_seq OWNER TO admin;

--
-- Name: studies_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: admin
--

ALTER SEQUENCE public.studies_id_seq OWNED BY public.studies.id;


--
-- Name: studies_researcher_account_ids; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.studies_researcher_account_ids (
    studies_id integer NOT NULL,
    researcher_account_ids character varying(255)
);


ALTER TABLE public.studies_researcher_account_ids OWNER TO admin;

--
-- Name: summary; Type: TABLE; Schema: public; Owner: admin
--

CREATE TABLE public.summary (
    id character varying(255) NOT NULL,
    created_at timestamp without time zone,
    created_by character varying(255),
    updated_at timestamp without time zone,
    updated_by character varying(255),
    file_name character varying(255) NOT NULL,
    status character varying(255),
    study_id character varying(255)
);


ALTER TABLE public.summary OWNER TO admin;

--
-- Name: collections id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.collections ALTER COLUMN id SET DEFAULT nextval('public.collections_id_seq'::regclass);


--
-- Name: consent_documents id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.consent_documents ALTER COLUMN id SET DEFAULT nextval('public.consent_documents_id_seq'::regclass);


--
-- Name: data_points id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.data_points ALTER COLUMN id SET DEFAULT nextval('public.data_points_id_seq'::regclass);


--
-- Name: data_stream_ids id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.data_stream_ids ALTER COLUMN id SET DEFAULT nextval('public.data_stream_ids_id_seq'::regclass);


--
-- Name: data_stream_sequence id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.data_stream_sequence ALTER COLUMN id SET DEFAULT nextval('public.data_stream_sequence_id_seq'::regclass);


--
-- Name: deployments id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.deployments ALTER COLUMN id SET DEFAULT nextval('public.deployments_id_seq'::regclass);


--
-- Name: documents id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.documents ALTER COLUMN id SET DEFAULT nextval('public.documents_id_seq'::regclass);


--
-- Name: files id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.files ALTER COLUMN id SET DEFAULT nextval('public.files_id_seq'::regclass);


--
-- Name: participant_groups id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.participant_groups ALTER COLUMN id SET DEFAULT nextval('public.participant_groups_id_seq'::regclass);


--
-- Name: protocols id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.protocols ALTER COLUMN id SET DEFAULT nextval('public.protocols_id_seq'::regclass);


--
-- Name: recruitments id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.recruitments ALTER COLUMN id SET DEFAULT nextval('public.recruitments_id_seq'::regclass);


--
-- Name: studies id; Type: DEFAULT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.studies ALTER COLUMN id SET DEFAULT nextval('public.studies_id_seq'::regclass);


--
-- Name: collections collections_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT collections_pkey PRIMARY KEY (id);


--
-- Name: consent_documents consent_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.consent_documents
    ADD CONSTRAINT consent_documents_pkey PRIMARY KEY (id);


--
-- Name: data_points data_points_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.data_points
    ADD CONSTRAINT data_points_pkey PRIMARY KEY (id);


--
-- Name: data_stream_configurations data_stream_configurations_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.data_stream_configurations
    ADD CONSTRAINT data_stream_configurations_pkey PRIMARY KEY (study_deployment_id);


--
-- Name: data_stream_ids data_stream_ids_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.data_stream_ids
    ADD CONSTRAINT data_stream_ids_pkey PRIMARY KEY (id);


--
-- Name: data_stream_sequence data_stream_sequence_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.data_stream_sequence
    ADD CONSTRAINT data_stream_sequence_pkey PRIMARY KEY (id);


--
-- Name: deployments deployments_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.deployments
    ADD CONSTRAINT deployments_pkey PRIMARY KEY (id);


--
-- Name: documents documents_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT documents_pkey PRIMARY KEY (id);


--
-- Name: files files_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.files
    ADD CONSTRAINT files_pkey PRIMARY KEY (id);


--
-- Name: participant_groups participant_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.participant_groups
    ADD CONSTRAINT participant_groups_pkey PRIMARY KEY (id);


--
-- Name: protocols protocols_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.protocols
    ADD CONSTRAINT protocols_pkey PRIMARY KEY (id);


--
-- Name: recruitments recruitments_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.recruitments
    ADD CONSTRAINT recruitments_pkey PRIMARY KEY (id);


--
-- Name: studies studies_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.studies
    ADD CONSTRAINT studies_pkey PRIMARY KEY (id);


--
-- Name: summary summary_pkey; Type: CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.summary
    ADD CONSTRAINT summary_pkey PRIMARY KEY (id);


--
-- Name: data_stream_sequence fk_data_stream_sequence_data_str_id; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.data_stream_sequence
    ADD CONSTRAINT fk_data_stream_sequence_data_str_id FOREIGN KEY (data_stream_id) REFERENCES public.data_stream_ids(id);


--
-- Name: collections fkejs0a68wa4v7635m33hm1lnkc; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.collections
    ADD CONSTRAINT fkejs0a68wa4v7635m33hm1lnkc FOREIGN KEY (document_id) REFERENCES public.documents(id);


--
-- Name: studies_researcher_account_ids fkhistgt1k4w22cv4vtf0eay9to; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.studies_researcher_account_ids
    ADD CONSTRAINT fkhistgt1k4w22cv4vtf0eay9to FOREIGN KEY (studies_id) REFERENCES public.studies(id) ON DELETE CASCADE;


--
-- Name: documents fkisfxu0tmysel9d9dklmupqlke; Type: FK CONSTRAINT; Schema: public; Owner: admin
--

ALTER TABLE ONLY public.documents
    ADD CONSTRAINT fkisfxu0tmysel9d9dklmupqlke FOREIGN KEY (collection_id) REFERENCES public.collections(id);


--
-- PostgreSQL database dump complete
--

