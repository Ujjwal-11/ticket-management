package com.ticketmanagement.ticket.persistence;

import com.ticketmanagement.ticket.domain.TicketStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

  @EntityGraph(attributePaths = "comments")
  @Query("select t from TicketEntity t where t.id = :id")
  Optional<TicketEntity> findWithCommentsById(@Param("id") Long id);

  @Query(
      """
      select t from TicketEntity t
      where (
        cast(:q as string) is null
        or lower(t.title) like lower(concat('%', cast(:q as string), '%'))
        or lower(t.description) like lower(concat('%', cast(:q as string), '%'))
      )
      and (:status is null or t.status = :status)
      """)
  Page<TicketEntity> search(
      @Param("q") String q, @Param("status") TicketStatus status, Pageable pageable);
}
