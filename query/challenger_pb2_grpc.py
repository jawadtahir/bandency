# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
"""Client and server classes corresponding to protobuf-defined services."""
import grpc

import challenger_pb2 as challenger__pb2
from google.protobuf import empty_pb2 as google_dot_protobuf_dot_empty__pb2


class ChallengerStub(object):
    """Missing associated documentation comment in .proto file."""

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.getLocations = channel.unary_unary(
                '/Challenger.Challenger/getLocations',
                request_serializer=google_dot_protobuf_dot_empty__pb2.Empty.SerializeToString,
                response_deserializer=challenger__pb2.Locations.FromString,
                )
        self.createNewBenchmark = channel.unary_unary(
                '/Challenger.Challenger/createNewBenchmark',
                request_serializer=challenger__pb2.BenchmarkConfiguration.SerializeToString,
                response_deserializer=challenger__pb2.Benchmark.FromString,
                )
        self.initializeLatencyMeasuring = channel.unary_unary(
                '/Challenger.Challenger/initializeLatencyMeasuring',
                request_serializer=challenger__pb2.Benchmark.SerializeToString,
                response_deserializer=challenger__pb2.Ping.FromString,
                )
        self.measure = channel.unary_unary(
                '/Challenger.Challenger/measure',
                request_serializer=challenger__pb2.Ping.SerializeToString,
                response_deserializer=challenger__pb2.Ping.FromString,
                )
        self.endMeasurement = channel.unary_unary(
                '/Challenger.Challenger/endMeasurement',
                request_serializer=challenger__pb2.Ping.SerializeToString,
                response_deserializer=google_dot_protobuf_dot_empty__pb2.Empty.FromString,
                )
        self.startBenchmark = channel.unary_unary(
                '/Challenger.Challenger/startBenchmark',
                request_serializer=challenger__pb2.Benchmark.SerializeToString,
                response_deserializer=google_dot_protobuf_dot_empty__pb2.Empty.FromString,
                )
        self.nextMessage = channel.unary_unary(
                '/Challenger.Challenger/nextMessage',
                request_serializer=challenger__pb2.Benchmark.SerializeToString,
                response_deserializer=challenger__pb2.Batch.FromString,
                )
        self.resultQ1 = channel.unary_unary(
                '/Challenger.Challenger/resultQ1',
                request_serializer=challenger__pb2.ResultQ1.SerializeToString,
                response_deserializer=google_dot_protobuf_dot_empty__pb2.Empty.FromString,
                )
        self.resultQ2 = channel.unary_unary(
                '/Challenger.Challenger/resultQ2',
                request_serializer=challenger__pb2.ResultQ2.SerializeToString,
                response_deserializer=google_dot_protobuf_dot_empty__pb2.Empty.FromString,
                )
        self.endBenchmark = channel.unary_unary(
                '/Challenger.Challenger/endBenchmark',
                request_serializer=challenger__pb2.Benchmark.SerializeToString,
                response_deserializer=google_dot_protobuf_dot_empty__pb2.Empty.FromString,
                )


class ChallengerServicer(object):
    """Missing associated documentation comment in .proto file."""

    def getLocations(self, request, context):
        """Get the polygons of all zip areas in germany
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def createNewBenchmark(self, request, context):
        """Create a new Benchmark based on the configuration
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def initializeLatencyMeasuring(self, request, context):
        """Depending on your connectivity you have a latency and throughput.
        Optionally, we try to account for this by first measuring it.
        The payload of a Ping corresponds roughly to the payload of a batch and the returning Pong roughly the payload of a Result
        This kind of measurement is just for development and experimentation (since it could be easily cheated ;-))
        We do not consider that once you deploy your implementation on the VMs in our infrastructure
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def measure(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def endMeasurement(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def startBenchmark(self, request, context):
        """This marks the starting point of the throughput measurements
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def nextMessage(self, request, context):
        """get the next Batch
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def resultQ1(self, request, context):
        """post the result
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def resultQ2(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def endBenchmark(self, request, context):
        """This marks the end of the throughput measurements
        """
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_ChallengerServicer_to_server(servicer, server):
    rpc_method_handlers = {
            'getLocations': grpc.unary_unary_rpc_method_handler(
                    servicer.getLocations,
                    request_deserializer=google_dot_protobuf_dot_empty__pb2.Empty.FromString,
                    response_serializer=challenger__pb2.Locations.SerializeToString,
            ),
            'createNewBenchmark': grpc.unary_unary_rpc_method_handler(
                    servicer.createNewBenchmark,
                    request_deserializer=challenger__pb2.BenchmarkConfiguration.FromString,
                    response_serializer=challenger__pb2.Benchmark.SerializeToString,
            ),
            'initializeLatencyMeasuring': grpc.unary_unary_rpc_method_handler(
                    servicer.initializeLatencyMeasuring,
                    request_deserializer=challenger__pb2.Benchmark.FromString,
                    response_serializer=challenger__pb2.Ping.SerializeToString,
            ),
            'measure': grpc.unary_unary_rpc_method_handler(
                    servicer.measure,
                    request_deserializer=challenger__pb2.Ping.FromString,
                    response_serializer=challenger__pb2.Ping.SerializeToString,
            ),
            'endMeasurement': grpc.unary_unary_rpc_method_handler(
                    servicer.endMeasurement,
                    request_deserializer=challenger__pb2.Ping.FromString,
                    response_serializer=google_dot_protobuf_dot_empty__pb2.Empty.SerializeToString,
            ),
            'startBenchmark': grpc.unary_unary_rpc_method_handler(
                    servicer.startBenchmark,
                    request_deserializer=challenger__pb2.Benchmark.FromString,
                    response_serializer=google_dot_protobuf_dot_empty__pb2.Empty.SerializeToString,
            ),
            'nextMessage': grpc.unary_unary_rpc_method_handler(
                    servicer.nextMessage,
                    request_deserializer=challenger__pb2.Benchmark.FromString,
                    response_serializer=challenger__pb2.Batch.SerializeToString,
            ),
            'resultQ1': grpc.unary_unary_rpc_method_handler(
                    servicer.resultQ1,
                    request_deserializer=challenger__pb2.ResultQ1.FromString,
                    response_serializer=google_dot_protobuf_dot_empty__pb2.Empty.SerializeToString,
            ),
            'resultQ2': grpc.unary_unary_rpc_method_handler(
                    servicer.resultQ2,
                    request_deserializer=challenger__pb2.ResultQ2.FromString,
                    response_serializer=google_dot_protobuf_dot_empty__pb2.Empty.SerializeToString,
            ),
            'endBenchmark': grpc.unary_unary_rpc_method_handler(
                    servicer.endBenchmark,
                    request_deserializer=challenger__pb2.Benchmark.FromString,
                    response_serializer=google_dot_protobuf_dot_empty__pb2.Empty.SerializeToString,
            ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
            'Challenger.Challenger', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))


 # This class is part of an EXPERIMENTAL API.
class Challenger(object):
    """Missing associated documentation comment in .proto file."""

    @staticmethod
    def getLocations(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/Challenger.Challenger/getLocations',
            google_dot_protobuf_dot_empty__pb2.Empty.SerializeToString,
            challenger__pb2.Locations.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def createNewBenchmark(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/Challenger.Challenger/createNewBenchmark',
            challenger__pb2.BenchmarkConfiguration.SerializeToString,
            challenger__pb2.Benchmark.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def initializeLatencyMeasuring(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/Challenger.Challenger/initializeLatencyMeasuring',
            challenger__pb2.Benchmark.SerializeToString,
            challenger__pb2.Ping.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def measure(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/Challenger.Challenger/measure',
            challenger__pb2.Ping.SerializeToString,
            challenger__pb2.Ping.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def endMeasurement(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/Challenger.Challenger/endMeasurement',
            challenger__pb2.Ping.SerializeToString,
            google_dot_protobuf_dot_empty__pb2.Empty.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def startBenchmark(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/Challenger.Challenger/startBenchmark',
            challenger__pb2.Benchmark.SerializeToString,
            google_dot_protobuf_dot_empty__pb2.Empty.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def nextMessage(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/Challenger.Challenger/nextMessage',
            challenger__pb2.Benchmark.SerializeToString,
            challenger__pb2.Batch.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def resultQ1(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/Challenger.Challenger/resultQ1',
            challenger__pb2.ResultQ1.SerializeToString,
            google_dot_protobuf_dot_empty__pb2.Empty.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def resultQ2(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/Challenger.Challenger/resultQ2',
            challenger__pb2.ResultQ2.SerializeToString,
            google_dot_protobuf_dot_empty__pb2.Empty.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def endBenchmark(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            insecure=False,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/Challenger.Challenger/endBenchmark',
            challenger__pb2.Benchmark.SerializeToString,
            google_dot_protobuf_dot_empty__pb2.Empty.FromString,
            options, channel_credentials,
            insecure, call_credentials, compression, wait_for_ready, timeout, metadata)
