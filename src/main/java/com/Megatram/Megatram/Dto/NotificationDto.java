package com.Megatram.Megatram.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class NotificationDto {
    private Long id;
    private String type;
    private String message;
    private Long userId;

    @JsonProperty("commandeId")
    private Long infoId;

    @JsonProperty("statut")
    private String infoStatus;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime date;

    private boolean lu;

    // Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getInfoId() { return infoId; }
    public void setInfoId(Long infoId) { this.infoId = infoId; }

    public String getInfoStatus() { return infoStatus; }
    public void setInfoStatus(String infoStatus) { this.infoStatus = infoStatus; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public boolean isLu() { return lu; }
    public void setLu(boolean lu) { this.lu = lu; }
}
