#[path = "../encoder/mod.rs"]
mod encoder;

use encoder::mf_encoder::HardwareH264Encoder;

fn main() {
    println!("Creating HardwareH264Encoder 1366x768...");
    let mut enc = match HardwareH264Encoder::new(1366, 768, 30, 2000000) {
        Ok(e) => e,
        Err(e) => {
            eprintln!("Failed to create encoder: {}", e);
            return;
        }
    };
    println!("Encoder created! Now encoding 10 test frames...");
    let dummy_rgba = vec![128u8; 1366 * 768 * 4];
    for i in 0..10 {
        let is_key = i == 0;
        let start = std::time::Instant::now();
        match enc.encode_rgba(&dummy_rgba, 1366, 768, is_key) {
            Ok(bytes) => {
                println!("Frame {}: encoded bytes = {}, time = {:?}", i, bytes.len(), start.elapsed());
            }
            Err(e) => {
                eprintln!("Frame {}: error = {}", i, e);
            }
        }
        std::thread::sleep(std::time::Duration::from_millis(33));
    }
}
