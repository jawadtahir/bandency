pub mod grpc;

use axum::{
    extract::{Json, State},
    http::StatusCode,
    routing::post,
    Router,
};

use grpc::Pool;

use std::net::SocketAddr;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

use api::*;
mod api;

#[tokio::main]
async fn main() {
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "proxy=info".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    if std::env::args().len() != 3 {
        eprintln!("USAGE: proxy BIND_ADDRESS BACKEND_ADDRESS");
        std::process::exit(1);
    }

    let bind_addr = std::env::args().nth(1).unwrap();
    let backend_addr = std::env::args().nth(2).unwrap();

    let pool = Pool::builder(grpc::GrpcPoolManager::new(&backend_addr))
        .max_size(128)
        .build()
        .unwrap();

    // ROUTES
    let app = Router::new()
        .route("/create", post(create_new_benchmark))
        .route("/start", post(start_benchmark))
        .route("/end", post(end_benchmark))
        .route("/next_batch", post(next_batch))
        .route("/result_q1", post(result_q1))
        .route("/result_q2", post(result_q2))
        .with_state(pool.clone());

    let addr: SocketAddr = bind_addr.parse().expect("invalid bind address");
    tracing::info!("starting httprpc with bind_address: {addr}, backend_address: {backend_addr}");
    axum_server::bind(addr)
        .serve(app.into_make_service())
        .await
        .unwrap();
}

// ENDPOINTS

async fn create_new_benchmark(
    State(pool): State<Pool>,
    Json(body): Json<BenchmarkConfiguration>,
) -> Result<Json<Benchmark>, (StatusCode, String)> {
    let r = pool
        .get()
        .await
        .unwrap()
        .create_new_benchmark(body)
        .await
        .unwrap();
    Ok(Json(r.into_inner()))
}

async fn start_benchmark(
    State(pool): State<Pool>,
    Json(body): Json<Benchmark>,
) -> Result<(), (StatusCode, String)> {
    pool.get()
        .await
        .unwrap()
        .start_benchmark(body)
        .await
        .unwrap();
    Ok(())
}

async fn end_benchmark(
    State(pool): State<Pool>,
    Json(body): Json<Benchmark>,
) -> Result<(), (StatusCode, String)> {
    pool.get().await.unwrap().end_benchmark(body).await.unwrap();
    Ok(())
}

async fn next_batch(
    State(pool): State<Pool>,
    Json(body): Json<Benchmark>,
) -> Result<Json<Batch>, (StatusCode, String)> {
    let r = pool.get().await.unwrap().next_batch(body).await.unwrap();
    Ok(Json(r.into_inner()))
}

async fn result_q1(
    State(pool): State<Pool>,
    Json(body): Json<ResultQ1>,
) -> Result<(), (StatusCode, String)> {
    pool.get().await.unwrap().result_q1(body).await.unwrap();
    Ok(())
}

async fn result_q2(
    State(pool): State<Pool>,
    Json(body): Json<ResultQ2>,
) -> Result<(), (StatusCode, String)> {
    pool.get().await.unwrap().result_q2(body).await.unwrap();
    Ok(())
}
