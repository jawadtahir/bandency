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
-- Name: benchmarkresults1; Type: TABLE; Schema: public; Owner: bandency
--

CREATE TABLE public.benchmarkresults1 (
    id bigint,
    duration_sec double precision,
    q1_count bigint,
    q1_failurecount bigint,
    q1_postfailurecount bigint,
    q1_throughput double precision,
    q1_failurethroughput double precision,
    q1_postfailurethroughput double precision,
    q1_90percentile double precision,
    q1_failure90percentile double precision,
    q1_postfailure90percentile double precision,
    q2_count bigint,
    q2_failurecount bigint,
    q2_postfailurecount bigint,
    q2_throughput double precision,
    q2_failurethroughput double precision,
    q2_postfailurethroughtput double precision,
    q2_90percentile double precision,
    q2failure90percentile double precision,
    q2postfailure90percentile double precision,
    summary text
);


ALTER TABLE public.benchmarkresults1 OWNER TO bandency;

--###################################################################################



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
-- Name: querymetrics1; Type: TABLE; Schema: public; Owner: bandency
--

CREATE TABLE public.querymetrics1 (
    benchmark_id bigint,
    batch_id bigint,
    starttime bigint,
    q1resulttime bigint,
    q1latency bigint,
    q1failureresulttime bigint,
    q1failurelatency bigint,
    q1postfailureresulttime bigint,
    q1postfailurelatency bigint,
    q2resulttime bigint,
    q2latency bigint,
    q2failureresulttime bigint,
    q2failurelatency bigint,
    q2postfailureresulttime bigint,
    q2postfailurelatency bigint
);


ALTER TABLE public.querymetrics1 OWNER TO bandency;

