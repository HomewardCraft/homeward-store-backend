package com.homeward.website.service;

import com.homeward.website.bean.PO.ItemCart;
import com.homeward.website.mapper.CartMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CartMapper cartMapper;

    public CartServiceImpl(RedisTemplate<String, Object> redisTemplate, CartMapper cartMapper) {
        this.redisTemplate = redisTemplate;
        this.cartMapper = cartMapper;
    }

    @Override
    public Long cartIncrease(String playerId, String packageId) {
        if (!itemIsExist(packageId)) return null;
        String playerName = (String) redisTemplate.opsForValue().get(playerId);
        if (playerName == null) return null;
        if (!redisTemplate.opsForHash().hasKey(playerName, packageId)) {
            redisTemplate.opsForHash().putIfAbsent(playerName, packageId, 1L);
        } else {
            redisTemplate.opsForHash().increment(playerName, packageId, 1L);
        }
        return Long.parseLong(redisTemplate.opsForHash().get(playerName, packageId) + "");
    }

    @Override
    public Long cartDecrease(String playerId, String packageId) {
        if (!itemIsExist(packageId)) return null;
        String playerName = (String) redisTemplate.opsForValue().get(playerId);
        if (playerName == null) return null;
        if (!redisTemplate.opsForHash().hasKey(playerName, packageId)) return null;
        long current = Long.parseLong(redisTemplate.opsForHash().get(playerName, packageId) + "");
        if (current == 1L) {
            redisTemplate.opsForHash().delete(playerName, packageId);
            return 0L;
        }
        redisTemplate.opsForHash().increment(playerName, packageId, -1);
        return current -1L;
    }

    @Override
    public Boolean itemReset(String playerId, String packageId) {
        String playerName = (String) redisTemplate.opsForValue().get(playerId);
        if (playerName == null) return null;
        return redisTemplate.opsForHash().delete(playerName, packageId) == 1L;
    }

    @Override
    public List<ItemCart> cartPreview(String playerId) {
        String playerName = (String) redisTemplate.opsForValue().get(playerId);
        if (playerName == null) return null;
        List<ItemCart> list = new ArrayList<>();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(playerName);
        entries.forEach((k, v) -> {
            ItemCart itemCart = (ItemCart) redisTemplate.opsForHash().get("ItemCart", k);
            assert itemCart != null;
            itemCart.setInCart(Long.parseLong(v + ""));
            list.add(itemCart);
        });
        return list;
    }

    Boolean itemIsExist(String packageId) {
        if (!redisTemplate.opsForHash().hasKey("ItemCart", packageId)) {
            ItemCart itemCart = cartMapper.selectItemCartDetailById(packageId);
            if (itemCart == null) return false;
            redisTemplate.opsForHash().putIfAbsent("ItemCart", packageId, itemCart);
        }
        return true;
    }
}
