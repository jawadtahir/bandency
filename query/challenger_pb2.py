# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: challenger.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


from google.protobuf import empty_pb2 as google_dot_protobuf_dot_empty__pb2
from google.protobuf import timestamp_pb2 as google_dot_protobuf_dot_timestamp__pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='challenger.proto',
  package='Challenger',
  syntax='proto3',
  serialized_options=b'\n\023de.tum.i13.bandencyB\017ChallengerProtoP\001',
  create_key=_descriptor._internal_create_key,
  serialized_pb=b'\n\x10\x63hallenger.proto\x12\nChallenger\x1a\x1bgoogle/protobuf/empty.proto\x1a\x1fgoogle/protobuf/timestamp.proto\"y\n\x0bMeasurement\x12-\n\ttimestamp\x18\x01 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x10\n\x08latitude\x18\x02 \x01(\x02\x12\x11\n\tlongitude\x18\x03 \x01(\x02\x12\n\n\x02p1\x18\x04 \x01(\x02\x12\n\n\x02p2\x18\x05 \x01(\x02\"z\n\x05\x42\x61tch\x12\x0e\n\x06seq_id\x18\x01 \x01(\x03\x12\x0c\n\x04last\x18\x02 \x01(\x08\x12(\n\x07\x63urrent\x18\x03 \x03(\x0b\x32\x17.Challenger.Measurement\x12)\n\x08lastyear\x18\x04 \x03(\x0b\x32\x17.Challenger.Measurement\"\x17\n\tBenchmark\x12\n\n\x02id\x18\x01 \x01(\x03\"w\n\nTopKCities\x12\x10\n\x08position\x18\x01 \x01(\x05\x12\x0c\n\x04\x63ity\x18\x02 \x01(\t\x12\x1d\n\x15\x61verageAQIImprovement\x18\x03 \x01(\x05\x12\x14\n\x0c\x63urrentAQIP1\x18\x05 \x01(\x05\x12\x14\n\x0c\x63urrentAQIP2\x18\x06 \x01(\x05\"d\n\x08ResultQ1\x12\x14\n\x0c\x62\x65nchmark_id\x18\x01 \x01(\x03\x12\x14\n\x0c\x62\x61tch_seq_id\x18\x02 \x01(\x03\x12,\n\x0ctopkimproved\x18\x03 \x03(\x0b\x32\x16.Challenger.TopKCities\"M\n\x0bTopKStreaks\x12\x13\n\x0b\x62ucket_from\x18\x01 \x01(\x05\x12\x11\n\tbucket_to\x18\x02 \x01(\x05\x12\x16\n\x0e\x62ucket_percent\x18\x03 \x01(\x05\"b\n\x08ResultQ2\x12\x14\n\x0c\x62\x65nchmark_id\x18\x01 \x01(\x03\x12\x14\n\x0c\x62\x61tch_seq_id\x18\x02 \x01(\x03\x12*\n\thistogram\x18\x03 \x03(\x0b\x32\x17.Challenger.TopKStreaks\"4\n\x04Ping\x12\x14\n\x0c\x62\x65nchmark_id\x18\x01 \x01(\x03\x12\x16\n\x0e\x63orrelation_id\x18\x02 \x01(\x03\"\xbf\x01\n\x16\x42\x65nchmarkConfiguration\x12\r\n\x05token\x18\x01 \x01(\t\x12\x12\n\nbatch_size\x18\x02 \x01(\x05\x12\x16\n\x0e\x62\x65nchmark_name\x18\x03 \x01(\t\x12\x16\n\x0e\x62\x65nchmark_type\x18\x04 \x01(\t\x12\x39\n\x07queries\x18\x05 \x03(\x0e\x32(.Challenger.BenchmarkConfiguration.Query\"\x17\n\x05Query\x12\x06\n\x02Q1\x10\x00\x12\x06\n\x02Q2\x10\x01\",\n\x05Point\x12\x11\n\tlongitude\x18\x01 \x01(\x01\x12\x10\n\x08latitude\x18\x02 \x01(\x01\",\n\x07Polygon\x12!\n\x06points\x18\x01 \x03(\x0b\x32\x11.Challenger.Point\"q\n\x08Location\x12\x0f\n\x07zipcode\x18\x01 \x01(\t\x12\x0c\n\x04\x63ity\x18\x02 \x01(\t\x12\x0b\n\x03qkm\x18\x03 \x01(\x01\x12\x12\n\npopulation\x18\x04 \x01(\x05\x12%\n\x08polygons\x18\x05 \x03(\x0b\x32\x13.Challenger.Polygon\"4\n\tLocations\x12\'\n\tlocations\x18\x01 \x03(\x0b\x32\x14.Challenger.Location2\xf8\x04\n\nChallenger\x12O\n\x12\x63reateNewBenchmark\x12\".Challenger.BenchmarkConfiguration\x1a\x15.Challenger.Benchmark\x12<\n\x0cgetLocations\x12\x15.Challenger.Benchmark\x1a\x15.Challenger.Locations\x12\x45\n\x1ainitializeLatencyMeasuring\x12\x15.Challenger.Benchmark\x1a\x10.Challenger.Ping\x12-\n\x07measure\x12\x10.Challenger.Ping\x1a\x10.Challenger.Ping\x12:\n\x0e\x65ndMeasurement\x12\x10.Challenger.Ping\x1a\x16.google.protobuf.Empty\x12?\n\x0estartBenchmark\x12\x15.Challenger.Benchmark\x1a\x16.google.protobuf.Empty\x12\x35\n\tnextBatch\x12\x15.Challenger.Benchmark\x1a\x11.Challenger.Batch\x12\x38\n\x08resultQ1\x12\x14.Challenger.ResultQ1\x1a\x16.google.protobuf.Empty\x12\x38\n\x08resultQ2\x12\x14.Challenger.ResultQ2\x1a\x16.google.protobuf.Empty\x12=\n\x0c\x65ndBenchmark\x12\x15.Challenger.Benchmark\x1a\x16.google.protobuf.EmptyB(\n\x13\x64\x65.tum.i13.bandencyB\x0f\x43hallengerProtoP\x01\x62\x06proto3'
  ,
  dependencies=[google_dot_protobuf_dot_empty__pb2.DESCRIPTOR,google_dot_protobuf_dot_timestamp__pb2.DESCRIPTOR,])



_BENCHMARKCONFIGURATION_QUERY = _descriptor.EnumDescriptor(
  name='Query',
  full_name='Challenger.BenchmarkConfiguration.Query',
  filename=None,
  file=DESCRIPTOR,
  create_key=_descriptor._internal_create_key,
  values=[
    _descriptor.EnumValueDescriptor(
      name='Q1', index=0, number=0,
      serialized_options=None,
      type=None,
      create_key=_descriptor._internal_create_key),
    _descriptor.EnumValueDescriptor(
      name='Q2', index=1, number=1,
      serialized_options=None,
      type=None,
      create_key=_descriptor._internal_create_key),
  ],
  containing_type=None,
  serialized_options=None,
  serialized_start=991,
  serialized_end=1014,
)
_sym_db.RegisterEnumDescriptor(_BENCHMARKCONFIGURATION_QUERY)


_MEASUREMENT = _descriptor.Descriptor(
  name='Measurement',
  full_name='Challenger.Measurement',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='timestamp', full_name='Challenger.Measurement.timestamp', index=0,
      number=1, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='latitude', full_name='Challenger.Measurement.latitude', index=1,
      number=2, type=2, cpp_type=6, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='longitude', full_name='Challenger.Measurement.longitude', index=2,
      number=3, type=2, cpp_type=6, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='p1', full_name='Challenger.Measurement.p1', index=3,
      number=4, type=2, cpp_type=6, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='p2', full_name='Challenger.Measurement.p2', index=4,
      number=5, type=2, cpp_type=6, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=94,
  serialized_end=215,
)


_BATCH = _descriptor.Descriptor(
  name='Batch',
  full_name='Challenger.Batch',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='seq_id', full_name='Challenger.Batch.seq_id', index=0,
      number=1, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='last', full_name='Challenger.Batch.last', index=1,
      number=2, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='current', full_name='Challenger.Batch.current', index=2,
      number=3, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='lastyear', full_name='Challenger.Batch.lastyear', index=3,
      number=4, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=217,
  serialized_end=339,
)


_BENCHMARK = _descriptor.Descriptor(
  name='Benchmark',
  full_name='Challenger.Benchmark',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='id', full_name='Challenger.Benchmark.id', index=0,
      number=1, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=341,
  serialized_end=364,
)


_TOPKCITIES = _descriptor.Descriptor(
  name='TopKCities',
  full_name='Challenger.TopKCities',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='position', full_name='Challenger.TopKCities.position', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='city', full_name='Challenger.TopKCities.city', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='averageAQIImprovement', full_name='Challenger.TopKCities.averageAQIImprovement', index=2,
      number=3, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='currentAQIP1', full_name='Challenger.TopKCities.currentAQIP1', index=3,
      number=5, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='currentAQIP2', full_name='Challenger.TopKCities.currentAQIP2', index=4,
      number=6, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=366,
  serialized_end=485,
)


_RESULTQ1 = _descriptor.Descriptor(
  name='ResultQ1',
  full_name='Challenger.ResultQ1',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='benchmark_id', full_name='Challenger.ResultQ1.benchmark_id', index=0,
      number=1, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='batch_seq_id', full_name='Challenger.ResultQ1.batch_seq_id', index=1,
      number=2, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='topkimproved', full_name='Challenger.ResultQ1.topkimproved', index=2,
      number=3, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=487,
  serialized_end=587,
)


_TOPKSTREAKS = _descriptor.Descriptor(
  name='TopKStreaks',
  full_name='Challenger.TopKStreaks',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='bucket_from', full_name='Challenger.TopKStreaks.bucket_from', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='bucket_to', full_name='Challenger.TopKStreaks.bucket_to', index=1,
      number=2, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='bucket_percent', full_name='Challenger.TopKStreaks.bucket_percent', index=2,
      number=3, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=589,
  serialized_end=666,
)


_RESULTQ2 = _descriptor.Descriptor(
  name='ResultQ2',
  full_name='Challenger.ResultQ2',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='benchmark_id', full_name='Challenger.ResultQ2.benchmark_id', index=0,
      number=1, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='batch_seq_id', full_name='Challenger.ResultQ2.batch_seq_id', index=1,
      number=2, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='histogram', full_name='Challenger.ResultQ2.histogram', index=2,
      number=3, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=668,
  serialized_end=766,
)


_PING = _descriptor.Descriptor(
  name='Ping',
  full_name='Challenger.Ping',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='benchmark_id', full_name='Challenger.Ping.benchmark_id', index=0,
      number=1, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='correlation_id', full_name='Challenger.Ping.correlation_id', index=1,
      number=2, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=768,
  serialized_end=820,
)


_BENCHMARKCONFIGURATION = _descriptor.Descriptor(
  name='BenchmarkConfiguration',
  full_name='Challenger.BenchmarkConfiguration',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='token', full_name='Challenger.BenchmarkConfiguration.token', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='batch_size', full_name='Challenger.BenchmarkConfiguration.batch_size', index=1,
      number=2, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='benchmark_name', full_name='Challenger.BenchmarkConfiguration.benchmark_name', index=2,
      number=3, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='benchmark_type', full_name='Challenger.BenchmarkConfiguration.benchmark_type', index=3,
      number=4, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='queries', full_name='Challenger.BenchmarkConfiguration.queries', index=4,
      number=5, type=14, cpp_type=8, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _BENCHMARKCONFIGURATION_QUERY,
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=823,
  serialized_end=1014,
)


_POINT = _descriptor.Descriptor(
  name='Point',
  full_name='Challenger.Point',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='longitude', full_name='Challenger.Point.longitude', index=0,
      number=1, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='latitude', full_name='Challenger.Point.latitude', index=1,
      number=2, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1016,
  serialized_end=1060,
)


_POLYGON = _descriptor.Descriptor(
  name='Polygon',
  full_name='Challenger.Polygon',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='points', full_name='Challenger.Polygon.points', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1062,
  serialized_end=1106,
)


_LOCATION = _descriptor.Descriptor(
  name='Location',
  full_name='Challenger.Location',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='zipcode', full_name='Challenger.Location.zipcode', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='city', full_name='Challenger.Location.city', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='qkm', full_name='Challenger.Location.qkm', index=2,
      number=3, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='population', full_name='Challenger.Location.population', index=3,
      number=4, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='polygons', full_name='Challenger.Location.polygons', index=4,
      number=5, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1108,
  serialized_end=1221,
)


_LOCATIONS = _descriptor.Descriptor(
  name='Locations',
  full_name='Challenger.Locations',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='locations', full_name='Challenger.Locations.locations', index=0,
      number=1, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=1223,
  serialized_end=1275,
)

_MEASUREMENT.fields_by_name['timestamp'].message_type = google_dot_protobuf_dot_timestamp__pb2._TIMESTAMP
_BATCH.fields_by_name['current'].message_type = _MEASUREMENT
_BATCH.fields_by_name['lastyear'].message_type = _MEASUREMENT
_RESULTQ1.fields_by_name['topkimproved'].message_type = _TOPKCITIES
_RESULTQ2.fields_by_name['histogram'].message_type = _TOPKSTREAKS
_BENCHMARKCONFIGURATION.fields_by_name['queries'].enum_type = _BENCHMARKCONFIGURATION_QUERY
_BENCHMARKCONFIGURATION_QUERY.containing_type = _BENCHMARKCONFIGURATION
_POLYGON.fields_by_name['points'].message_type = _POINT
_LOCATION.fields_by_name['polygons'].message_type = _POLYGON
_LOCATIONS.fields_by_name['locations'].message_type = _LOCATION
DESCRIPTOR.message_types_by_name['Measurement'] = _MEASUREMENT
DESCRIPTOR.message_types_by_name['Batch'] = _BATCH
DESCRIPTOR.message_types_by_name['Benchmark'] = _BENCHMARK
DESCRIPTOR.message_types_by_name['TopKCities'] = _TOPKCITIES
DESCRIPTOR.message_types_by_name['ResultQ1'] = _RESULTQ1
DESCRIPTOR.message_types_by_name['TopKStreaks'] = _TOPKSTREAKS
DESCRIPTOR.message_types_by_name['ResultQ2'] = _RESULTQ2
DESCRIPTOR.message_types_by_name['Ping'] = _PING
DESCRIPTOR.message_types_by_name['BenchmarkConfiguration'] = _BENCHMARKCONFIGURATION
DESCRIPTOR.message_types_by_name['Point'] = _POINT
DESCRIPTOR.message_types_by_name['Polygon'] = _POLYGON
DESCRIPTOR.message_types_by_name['Location'] = _LOCATION
DESCRIPTOR.message_types_by_name['Locations'] = _LOCATIONS
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

Measurement = _reflection.GeneratedProtocolMessageType('Measurement', (_message.Message,), {
  'DESCRIPTOR' : _MEASUREMENT,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.Measurement)
  })
_sym_db.RegisterMessage(Measurement)

Batch = _reflection.GeneratedProtocolMessageType('Batch', (_message.Message,), {
  'DESCRIPTOR' : _BATCH,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.Batch)
  })
_sym_db.RegisterMessage(Batch)

Benchmark = _reflection.GeneratedProtocolMessageType('Benchmark', (_message.Message,), {
  'DESCRIPTOR' : _BENCHMARK,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.Benchmark)
  })
_sym_db.RegisterMessage(Benchmark)

TopKCities = _reflection.GeneratedProtocolMessageType('TopKCities', (_message.Message,), {
  'DESCRIPTOR' : _TOPKCITIES,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.TopKCities)
  })
_sym_db.RegisterMessage(TopKCities)

ResultQ1 = _reflection.GeneratedProtocolMessageType('ResultQ1', (_message.Message,), {
  'DESCRIPTOR' : _RESULTQ1,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.ResultQ1)
  })
_sym_db.RegisterMessage(ResultQ1)

TopKStreaks = _reflection.GeneratedProtocolMessageType('TopKStreaks', (_message.Message,), {
  'DESCRIPTOR' : _TOPKSTREAKS,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.TopKStreaks)
  })
_sym_db.RegisterMessage(TopKStreaks)

ResultQ2 = _reflection.GeneratedProtocolMessageType('ResultQ2', (_message.Message,), {
  'DESCRIPTOR' : _RESULTQ2,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.ResultQ2)
  })
_sym_db.RegisterMessage(ResultQ2)

Ping = _reflection.GeneratedProtocolMessageType('Ping', (_message.Message,), {
  'DESCRIPTOR' : _PING,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.Ping)
  })
_sym_db.RegisterMessage(Ping)

BenchmarkConfiguration = _reflection.GeneratedProtocolMessageType('BenchmarkConfiguration', (_message.Message,), {
  'DESCRIPTOR' : _BENCHMARKCONFIGURATION,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.BenchmarkConfiguration)
  })
_sym_db.RegisterMessage(BenchmarkConfiguration)

Point = _reflection.GeneratedProtocolMessageType('Point', (_message.Message,), {
  'DESCRIPTOR' : _POINT,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.Point)
  })
_sym_db.RegisterMessage(Point)

Polygon = _reflection.GeneratedProtocolMessageType('Polygon', (_message.Message,), {
  'DESCRIPTOR' : _POLYGON,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.Polygon)
  })
_sym_db.RegisterMessage(Polygon)

Location = _reflection.GeneratedProtocolMessageType('Location', (_message.Message,), {
  'DESCRIPTOR' : _LOCATION,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.Location)
  })
_sym_db.RegisterMessage(Location)

Locations = _reflection.GeneratedProtocolMessageType('Locations', (_message.Message,), {
  'DESCRIPTOR' : _LOCATIONS,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.Locations)
  })
_sym_db.RegisterMessage(Locations)


DESCRIPTOR._options = None

_CHALLENGER = _descriptor.ServiceDescriptor(
  name='Challenger',
  full_name='Challenger.Challenger',
  file=DESCRIPTOR,
  index=0,
  serialized_options=None,
  create_key=_descriptor._internal_create_key,
  serialized_start=1278,
  serialized_end=1910,
  methods=[
  _descriptor.MethodDescriptor(
    name='createNewBenchmark',
    full_name='Challenger.Challenger.createNewBenchmark',
    index=0,
    containing_service=None,
    input_type=_BENCHMARKCONFIGURATION,
    output_type=_BENCHMARK,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='getLocations',
    full_name='Challenger.Challenger.getLocations',
    index=1,
    containing_service=None,
    input_type=_BENCHMARK,
    output_type=_LOCATIONS,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='initializeLatencyMeasuring',
    full_name='Challenger.Challenger.initializeLatencyMeasuring',
    index=2,
    containing_service=None,
    input_type=_BENCHMARK,
    output_type=_PING,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='measure',
    full_name='Challenger.Challenger.measure',
    index=3,
    containing_service=None,
    input_type=_PING,
    output_type=_PING,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='endMeasurement',
    full_name='Challenger.Challenger.endMeasurement',
    index=4,
    containing_service=None,
    input_type=_PING,
    output_type=google_dot_protobuf_dot_empty__pb2._EMPTY,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='startBenchmark',
    full_name='Challenger.Challenger.startBenchmark',
    index=5,
    containing_service=None,
    input_type=_BENCHMARK,
    output_type=google_dot_protobuf_dot_empty__pb2._EMPTY,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='nextBatch',
    full_name='Challenger.Challenger.nextBatch',
    index=6,
    containing_service=None,
    input_type=_BENCHMARK,
    output_type=_BATCH,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='resultQ1',
    full_name='Challenger.Challenger.resultQ1',
    index=7,
    containing_service=None,
    input_type=_RESULTQ1,
    output_type=google_dot_protobuf_dot_empty__pb2._EMPTY,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='resultQ2',
    full_name='Challenger.Challenger.resultQ2',
    index=8,
    containing_service=None,
    input_type=_RESULTQ2,
    output_type=google_dot_protobuf_dot_empty__pb2._EMPTY,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='endBenchmark',
    full_name='Challenger.Challenger.endBenchmark',
    index=9,
    containing_service=None,
    input_type=_BENCHMARK,
    output_type=google_dot_protobuf_dot_empty__pb2._EMPTY,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
])
_sym_db.RegisterServiceDescriptor(_CHALLENGER)

DESCRIPTOR.services_by_name['Challenger'] = _CHALLENGER

# @@protoc_insertion_point(module_scope)
