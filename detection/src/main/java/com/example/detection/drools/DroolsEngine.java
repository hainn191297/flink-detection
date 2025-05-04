package com.example.detection.drools;

import java.util.List;

import com.example.detection.model.DroolsEvent;

/**
 * Sử dụng interface thay thế cho class phục vụ mục đích về sau muốn thay đổi
 * cách thức để filter
 */
public interface DroolsEngine {

    /**
     * Load hoặc reload toàn bộ rule engine từ một DRL string.
     * 
     * @param drl nội dung đầy đủ của rule dạng DRL
     */
    void reloadFromDRL(String drl);

    /**
     * Đánh giá một event đầu vào với toàn bộ rule hiện tại.
     * Trả về danh sách Rule đã match.
     * 
     * @param event Event đầu vào
     * @return danh sách rule match
     */
    List<String> evaluate(DroolsEvent event);
}
