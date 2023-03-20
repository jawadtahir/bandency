# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: challenger.proto
"""Generated protocol buffer code."""
from google.protobuf.internal import builder as _builder
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import empty_pb2 as google_dot_protobuf_dot_empty__pb2
from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x10\x63hallenger.proto\x12\nChallenger\x1a\x1bgoogle/protobuf/empty.proto\x1a\x1fgoogle/protobuf/timestamp.proto\"\xa6\x01\n\nDriveState\x12(\n\x04\x64\x61te\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x15\n\rserial_number\x18\x02 \x01(\t\x12\r\n\x05model\x18\x03 \x01(\t\x12\x16\n\x0e\x63\x61pacity_bytes\x18\x04 \x01(\x03\x12\x0f\n\x07\x66\x61ilure\x18\x05 \x01(\x05\x12\x12\n\nnormalized\x18\x06 \x01(\t\x12\x0b\n\x03raw\x18\x07 \x01(\t\"]\n\x05\x42\x61tch\x12\x0e\n\x06seq_id\x18\x01 \x01(\x03\x12\x0c\n\x04last\x18\x02 \x01(\x08\x12&\n\x06states\x18\x03 \x03(\x0b\x32\x16.Challenger.DriveState\x12\x0e\n\x06models\x18\x04 \x03(\t\"\x17\n\tBenchmark\x12\n\n\x02id\x18\x01 \x01(\x03\",\n\x08Outliers\x12\r\n\x05model\x18\x01 \x01(\t\x12\x11\n\tintervals\x18\x02 \x03(\t\"]\n\x08ResultQ1\x12\x14\n\x0c\x62\x65nchmark_id\x18\x01 \x01(\x03\x12\x14\n\x0c\x62\x61tch_seq_id\x18\x02 \x01(\x03\x12%\n\x07\x65ntries\x18\x03 \x03(\x0b\x32\x14.Challenger.Outliers\"c\n\x08ResultQ2\x12\x14\n\x0c\x62\x65nchmark_id\x18\x01 \x01(\x03\x12\x14\n\x0c\x62\x61tch_seq_id\x18\x02 \x01(\x03\x12\x15\n\rcentroids_out\x18\x03 \x03(\x03\x12\x14\n\x0c\x63\x65ntroids_in\x18\x04 \x03(\x03\"{\n\x16\x42\x65nchmarkConfiguration\x12\r\n\x05token\x18\x01 \x01(\t\x12\x16\n\x0e\x62\x65nchmark_name\x18\x02 \x01(\t\x12\x16\n\x0e\x62\x65nchmark_type\x18\x03 \x01(\t\x12\"\n\x07queries\x18\x04 \x03(\x0e\x32\x11.Challenger.Query*\x17\n\x05Query\x12\x06\n\x02Q1\x10\x00\x12\x06\n\x02Q2\x10\x01\x32\x88\x03\n\nChallenger\x12O\n\x12\x63reateNewBenchmark\x12\".Challenger.BenchmarkConfiguration\x1a\x15.Challenger.Benchmark\x12?\n\x0estartBenchmark\x12\x15.Challenger.Benchmark\x1a\x16.google.protobuf.Empty\x12\x35\n\tnextBatch\x12\x15.Challenger.Benchmark\x1a\x11.Challenger.Batch\x12\x38\n\x08resultQ1\x12\x14.Challenger.ResultQ1\x1a\x16.google.protobuf.Empty\x12\x38\n\x08resultQ2\x12\x14.Challenger.ResultQ2\x1a\x16.google.protobuf.Empty\x12=\n\x0c\x65ndBenchmark\x12\x15.Challenger.Benchmark\x1a\x16.google.protobuf.EmptyB\x17\n\x13\x64\x65.tum.i13.bandencyP\x01\x62\x06proto3')

_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, globals())
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'challenger_pb2', globals())
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\023de.tum.i13.bandencyP\001'
  _QUERY._serialized_start=750
  _QUERY._serialized_end=773
  _DRIVESTATE._serialized_start=95
  _DRIVESTATE._serialized_end=261
  _BATCH._serialized_start=263
  _BATCH._serialized_end=356
  _BENCHMARK._serialized_start=358
  _BENCHMARK._serialized_end=381
  _OUTLIERS._serialized_start=383
  _OUTLIERS._serialized_end=427
  _RESULTQ1._serialized_start=429
  _RESULTQ1._serialized_end=522
  _RESULTQ2._serialized_start=524
  _RESULTQ2._serialized_end=623
  _BENCHMARKCONFIGURATION._serialized_start=625
  _BENCHMARKCONFIGURATION._serialized_end=748
  _CHALLENGER._serialized_start=776
  _CHALLENGER._serialized_end=1168
# @@protoc_insertion_point(module_scope)
