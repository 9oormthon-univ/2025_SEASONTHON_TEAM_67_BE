package com.ohnew.ohnew.repository;

import com.ohnew.ohnew.entity.ChatRoom;
import com.ohnew.ohnew.entity.News;
import com.ohnew.ohnew.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByUserAndNews(User user, News news);
    List<ChatRoom> findByUserId(Long userId);

}