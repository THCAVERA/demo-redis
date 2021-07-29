package com.br.demo.redis.controller;

import com.br.demo.redis.payload.TestePayload;
import com.br.demo.redis.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teste")
public class RedisDemoController {

    @Autowired
    private CacheService cacheService;

    @PostMapping("/add/{key}")
    public void addCache(@PathVariable("key") String key){
        TestePayload orUpdate = cacheService.findOrUpdate(key, TestePayload.class,
                () -> TestePayload.builder().nome("Teste").build());
    }

    @GetMapping("/get/{key}")
    public String getCache(@PathVariable("key") String key){
       return cacheService.findCache(key);
    }
}
