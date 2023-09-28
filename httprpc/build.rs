fn main() -> Result<(), Box<dyn std::error::Error>> {
    let mut builder = tonic_build::configure().build_server(true);

    builder = builder
        // .out_dir("src")
        // .compile_well_known_types(true)
        .extern_path(".google.protobuf.Timestamp", "Timestamp")
        .type_attribute(".", "#[derive(serde::Serialize, serde::Deserialize)]");

    builder.compile(&["proto/challenger.proto"], &["proto/"])?;
    Ok(())
}
