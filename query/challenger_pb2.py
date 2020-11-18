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
  serialized_pb=b'\n\x10\x63hallenger.proto\x12\nChallenger\x1a\x1bgoogle/protobuf/empty.proto\x1a\x1fgoogle/protobuf/timestamp.proto\"\x89\x01\n\x07Payload\x12\x12\n\nrandomData\x18\x01 \x01(\x05\x12-\n\ttimestamp\x18\x02 \x01(\x0b\x32\x1a.google.protobuf.Timestamp\x12\x10\n\x08latitude\x18\x03 \x01(\x02\x12\x11\n\tlongitude\x18\x04 \x01(\x02\x12\n\n\x02p1\x18\x05 \x01(\x02\x12\n\n\x02p2\x18\x06 \x01(\x02\"d\n\x05\x42\x61tch\x12\x0e\n\x06seq_id\x18\x01 \x01(\x03\x12$\n\x07\x63urrent\x18\x02 \x03(\x0b\x32\x13.Challenger.Payload\x12%\n\x08lastyear\x18\x03 \x03(\x0b\x32\x13.Challenger.Payload\"\x17\n\tBenchmark\x12\n\n\x02id\x18\x01 \x01(\x03\"#\n\rResultPayload\x12\x12\n\nresultData\x18\x01 \x01(\x05\"a\n\x06Result\x12\x14\n\x0c\x62\x65nchmark_id\x18\x01 \x01(\x03\x12\x16\n\x0epayload_seq_id\x18\x02 \x01(\x03\x12)\n\x06result\x18\x03 \x01(\x0b\x32\x19.Challenger.ResultPayload\"4\n\x04Ping\x12\x14\n\x0c\x62\x65nchmark_id\x18\x01 \x01(\x03\x12\x16\n\x0e\x63orrelation_id\x18\x02 \x01(\x03\"S\n\x16\x42\x65nchmarkConfiguration\x12\r\n\x05token\x18\x01 \x01(\t\x12\x12\n\nbatch_size\x18\x02 \x01(\x05\x12\x16\n\x0e\x62\x65nchmark_name\x18\x03 \x01(\t\",\n\x05Point\x12\x11\n\tlongitude\x18\x01 \x01(\x01\x12\x10\n\x08latitude\x18\x02 \x01(\x01\"n\n\x08Location\x12\x0f\n\x07zipcode\x18\x01 \x01(\t\x12\x0c\n\x04\x63ity\x18\x02 \x01(\t\x12\x0b\n\x03qkm\x18\x03 \x01(\x01\x12\x12\n\npopulation\x18\x04 \x01(\x05\x12\"\n\x07polygon\x18\x05 \x03(\x0b\x32\x11.Challenger.Point\"4\n\tLocations\x12\'\n\tlocations\x18\x01 \x03(\x0b\x32\x14.Challenger.Location2\xc0\x04\n\nChallenger\x12=\n\x0cgetLocations\x12\x16.google.protobuf.Empty\x1a\x15.Challenger.Locations\x12O\n\x12\x63reateNewBenchmark\x12\".Challenger.BenchmarkConfiguration\x1a\x15.Challenger.Benchmark\x12\x45\n\x1ainitializeLatencyMeasuring\x12\x15.Challenger.Benchmark\x1a\x10.Challenger.Ping\x12-\n\x07measure\x12\x10.Challenger.Ping\x1a\x10.Challenger.Ping\x12:\n\x0e\x65ndMeasurement\x12\x10.Challenger.Ping\x1a\x16.google.protobuf.Empty\x12?\n\x0estartBenchmark\x12\x15.Challenger.Benchmark\x1a\x16.google.protobuf.Empty\x12\x37\n\x0bnextMessage\x12\x15.Challenger.Benchmark\x1a\x11.Challenger.Batch\x12\x37\n\tprocessed\x12\x12.Challenger.Result\x1a\x16.google.protobuf.Empty\x12=\n\x0c\x65ndBenchmark\x12\x15.Challenger.Benchmark\x1a\x16.google.protobuf.EmptyB(\n\x13\x64\x65.tum.i13.bandencyB\x0f\x43hallengerProtoP\x01\x62\x06proto3'
  ,
  dependencies=[google_dot_protobuf_dot_empty__pb2.DESCRIPTOR,google_dot_protobuf_dot_timestamp__pb2.DESCRIPTOR,])




_PAYLOAD = _descriptor.Descriptor(
  name='Payload',
  full_name='Challenger.Payload',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='randomData', full_name='Challenger.Payload.randomData', index=0,
      number=1, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='timestamp', full_name='Challenger.Payload.timestamp', index=1,
      number=2, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='latitude', full_name='Challenger.Payload.latitude', index=2,
      number=3, type=2, cpp_type=6, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='longitude', full_name='Challenger.Payload.longitude', index=3,
      number=4, type=2, cpp_type=6, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='p1', full_name='Challenger.Payload.p1', index=4,
      number=5, type=2, cpp_type=6, label=1,
      has_default_value=False, default_value=float(0),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='p2', full_name='Challenger.Payload.p2', index=5,
      number=6, type=2, cpp_type=6, label=1,
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
  serialized_start=95,
  serialized_end=232,
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
      name='current', full_name='Challenger.Batch.current', index=1,
      number=2, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='lastyear', full_name='Challenger.Batch.lastyear', index=2,
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
  serialized_start=234,
  serialized_end=334,
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
  serialized_start=336,
  serialized_end=359,
)


_RESULTPAYLOAD = _descriptor.Descriptor(
  name='ResultPayload',
  full_name='Challenger.ResultPayload',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='resultData', full_name='Challenger.ResultPayload.resultData', index=0,
      number=1, type=5, cpp_type=1, label=1,
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
  serialized_start=361,
  serialized_end=396,
)


_RESULT = _descriptor.Descriptor(
  name='Result',
  full_name='Challenger.Result',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='benchmark_id', full_name='Challenger.Result.benchmark_id', index=0,
      number=1, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='payload_seq_id', full_name='Challenger.Result.payload_seq_id', index=1,
      number=2, type=3, cpp_type=2, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
    _descriptor.FieldDescriptor(
      name='result', full_name='Challenger.Result.result', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
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
  serialized_start=398,
  serialized_end=495,
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
  serialized_start=497,
  serialized_end=549,
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
  serialized_start=551,
  serialized_end=634,
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
  serialized_start=636,
  serialized_end=680,
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
      name='polygon', full_name='Challenger.Location.polygon', index=4,
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
  serialized_start=682,
  serialized_end=792,
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
  serialized_start=794,
  serialized_end=846,
)

_PAYLOAD.fields_by_name['timestamp'].message_type = google_dot_protobuf_dot_timestamp__pb2._TIMESTAMP
_BATCH.fields_by_name['current'].message_type = _PAYLOAD
_BATCH.fields_by_name['lastyear'].message_type = _PAYLOAD
_RESULT.fields_by_name['result'].message_type = _RESULTPAYLOAD
_LOCATION.fields_by_name['polygon'].message_type = _POINT
_LOCATIONS.fields_by_name['locations'].message_type = _LOCATION
DESCRIPTOR.message_types_by_name['Payload'] = _PAYLOAD
DESCRIPTOR.message_types_by_name['Batch'] = _BATCH
DESCRIPTOR.message_types_by_name['Benchmark'] = _BENCHMARK
DESCRIPTOR.message_types_by_name['ResultPayload'] = _RESULTPAYLOAD
DESCRIPTOR.message_types_by_name['Result'] = _RESULT
DESCRIPTOR.message_types_by_name['Ping'] = _PING
DESCRIPTOR.message_types_by_name['BenchmarkConfiguration'] = _BENCHMARKCONFIGURATION
DESCRIPTOR.message_types_by_name['Point'] = _POINT
DESCRIPTOR.message_types_by_name['Location'] = _LOCATION
DESCRIPTOR.message_types_by_name['Locations'] = _LOCATIONS
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

Payload = _reflection.GeneratedProtocolMessageType('Payload', (_message.Message,), {
  'DESCRIPTOR' : _PAYLOAD,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.Payload)
  })
_sym_db.RegisterMessage(Payload)

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

ResultPayload = _reflection.GeneratedProtocolMessageType('ResultPayload', (_message.Message,), {
  'DESCRIPTOR' : _RESULTPAYLOAD,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.ResultPayload)
  })
_sym_db.RegisterMessage(ResultPayload)

Result = _reflection.GeneratedProtocolMessageType('Result', (_message.Message,), {
  'DESCRIPTOR' : _RESULT,
  '__module__' : 'challenger_pb2'
  # @@protoc_insertion_point(class_scope:Challenger.Result)
  })
_sym_db.RegisterMessage(Result)

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
  serialized_start=849,
  serialized_end=1425,
  methods=[
  _descriptor.MethodDescriptor(
    name='getLocations',
    full_name='Challenger.Challenger.getLocations',
    index=0,
    containing_service=None,
    input_type=google_dot_protobuf_dot_empty__pb2._EMPTY,
    output_type=_LOCATIONS,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='createNewBenchmark',
    full_name='Challenger.Challenger.createNewBenchmark',
    index=1,
    containing_service=None,
    input_type=_BENCHMARKCONFIGURATION,
    output_type=_BENCHMARK,
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
    name='nextMessage',
    full_name='Challenger.Challenger.nextMessage',
    index=6,
    containing_service=None,
    input_type=_BENCHMARK,
    output_type=_BATCH,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='processed',
    full_name='Challenger.Challenger.processed',
    index=7,
    containing_service=None,
    input_type=_RESULT,
    output_type=google_dot_protobuf_dot_empty__pb2._EMPTY,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
  _descriptor.MethodDescriptor(
    name='endBenchmark',
    full_name='Challenger.Challenger.endBenchmark',
    index=8,
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
