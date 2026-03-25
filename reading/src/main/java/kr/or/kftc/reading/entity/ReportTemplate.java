package kr.or.kftc.reading.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_template")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] fileData;

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private Admin uploadedBy;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
