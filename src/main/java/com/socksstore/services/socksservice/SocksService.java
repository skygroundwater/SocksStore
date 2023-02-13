package com.socksstore.services.socksservice;

import com.socksstore.models.socks.SocksEntity;
import org.springframework.web.multipart.MultipartFile;

public interface SocksService {
    void addSocksToStore(SocksEntity socks, Long quantity);

    void releaseSocksFromStore(SocksEntity socks, Long quantity);

    void writeOffSocksFromStore(SocksEntity socks, Long quantity, String cause);

    Long giveSameSocks(String color, Double size, Integer composition, Integer maxComposition);
}
