# HTTP REST to gRPC proxy for DEBS

```
USAGE: proxy BIND_ADDRESS BACKEND_ADDRESS
```

### Building

```
cargo build --release --bin proxy
``

### Editing

Uses the protobuf defined in `proto/challenger.proto`.

Edit the `tonic::include_proto!("challenger");` in `main.rs` if filename changes.
Edit `build.rs` to add serde derive macros if needed.
Edit the endpoints in the last part of `main.rs` to map the REST endpoints to gRPC calls.
Edit the routes in `async fn main` where the `Router` is created