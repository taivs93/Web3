package com.kunfeng2002.be002.controller;

import com.kunfeng2002.be002.dto.response.CoinResponse;
import com.kunfeng2002.be002.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/coins")
@RequiredArgsConstructor
public class CoinController {

    private final CoinService coinService;

    @GetMapping
    public ResponseEntity<List<CoinResponse>> getAllCoins() {
        List<CoinResponse> coins = coinService.getAllCoins();
        return ResponseEntity.ok(coins);
    }

    @GetMapping("/{address}")
    public ResponseEntity<CoinResponse> getCoinByAddress(@PathVariable String address) {
        return coinService.getCoinByAddress(address)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<CoinResponse>> getCoinsBySymbol(@PathVariable String symbol) {
        List<CoinResponse> coins = coinService.getCoinsBySymbol(symbol);
        return ResponseEntity.ok(coins);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CoinResponse>> searchCoins(@RequestParam String q) {
        List<CoinResponse> coins = coinService.searchCoins(q);
        return ResponseEntity.ok(coins);
    }


    @GetMapping("/search-online")
    public ResponseEntity<List<CoinResponse>> searchOnline(@RequestParam String q) {
        List<CoinResponse> coins = coinService.searchCoins(q);
        return ResponseEntity.ok(coins);
    }

    @GetMapping("/search-address-online")
    public ResponseEntity<CoinResponse> searchAddressOnline(@RequestParam String address) {
        Optional<CoinResponse> coin = coinService.getCoinByAddress(address);
        return coin.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/test-search")
    public ResponseEntity<String> testSearch() {
        return ResponseEntity.ok("""
                Chức năng search đã được tối ưu hóa! 
                
                CHỈ TÌM KIẾM TRỰC TUYẾN - HIỂN THỊ ĐƠN GIẢN
                
                Các endpoint có sẵn:
                - GET /coins/search?q={query} - Tìm kiếm tổng hợp theo name, symbol, address
                - GET /coins/{address} - Tìm kiếm theo địa chỉ contract
                - GET /coins/symbol/{symbol} - Tìm kiếm theo symbol
                - GET /coins/search-online?q={query} - Tìm kiếm trực tuyến
                - GET /coins/search-address-online?address={address} - Tìm kiếm địa chỉ trực tuyến
                
                Tìm kiếm theo địa chỉ contract:
                - /search 0x25d887Ce7a35172C62FeBFD67a1856F20FaEbB00
                - /search 0x524bC91Dc82d6b90EF29F76A3ECAaBAffFD490Bc
                
                Hiển thị kết quả đơn giản:
                - Name: Tên token
                - Address_Contract: Địa chỉ contract
                - Chỉ hiển thị coin có địa chỉ contract
                - Không hiển thị giá, market cap, volume
                - Không có icon/emoji
                
                Cải thiện:
                - Lọc bỏ coin không có Address_Contract
                - Tìm kiếm BSCScan trước cho địa chỉ BSC
                - Xử lý lỗi ClassCastException an toàn
                - Hiển thị kết quả gọn gàng, chỉ Name + Address
                - Tối ưu hóa cho tìm kiếm theo địa chỉ contract
                """);
    }
}
