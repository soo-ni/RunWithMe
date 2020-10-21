package kr.co.rwm.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder

public class Matching {
	
	@Id
	@Column(name = "room_id")
	private String roomId;
	
	@Column(name = "master_id", nullable = false)
	private Integer masterId;
	
	@Column(name = "guest_id", nullable = false)
	private Integer guestId;
	
}