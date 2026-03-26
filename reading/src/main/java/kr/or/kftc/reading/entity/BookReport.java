package kr.or.kftc.reading.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_report")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", unique = true, nullable = false)
    private CourseEnrollment enrollment;

    @Column(length = 200)
    private String title;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ReportStatus status;

    @Column(name = "supplement_reason", length = 500)
    private String supplementReason;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
