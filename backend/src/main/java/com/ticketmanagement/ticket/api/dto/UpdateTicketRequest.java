package com.ticketmanagement.ticket.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateTicketRequest {

  private String title;
  private boolean titlePresent;
  private String description;
  private boolean descriptionPresent;
  private String priority;
  private boolean priorityPresent;
  private String assignee;
  private boolean assigneePresent;
  private boolean statusPresent;

  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
    this.titlePresent = true;
  }

  public boolean isTitlePresent() {
    return titlePresent;
  }

  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
    this.descriptionPresent = true;
  }

  public boolean isDescriptionPresent() {
    return descriptionPresent;
  }

  public String getPriority() {
    return priority;
  }

  @JsonProperty("priority")
  public void setPriority(String priority) {
    this.priority = priority;
    this.priorityPresent = true;
  }

  public boolean isPriorityPresent() {
    return priorityPresent;
  }

  public String getAssignee() {
    return assignee;
  }

  @JsonProperty("assignee")
  public void setAssignee(String assignee) {
    this.assignee = assignee;
    this.assigneePresent = true;
  }

  public boolean isAssigneePresent() {
    return assigneePresent;
  }

  @JsonProperty("status")
  public void setStatus(String status) {
    this.statusPresent = true;
  }

  public boolean isStatusPresent() {
    return statusPresent;
  }
}
