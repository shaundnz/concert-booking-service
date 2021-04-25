package asg.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(name = "Performers")
public class Performer {
    
        @Id
      //  @GeneratedValue(strategy = GenerationType.IDENTITY)
        //automatically assigns a PK (id) to the object and inserts a value
        private Long id;
        private String name;
        @Column(name="IMAGE_NAME")
        private String imageUri;
    
        @Enumerated(EnumType.STRING)
        //Genre class is ordered alphabetically base on the Genre class
        private Genre genre;

        @Column(name="BLURB", length = 1000)
        private String blurb;

        @ManyToOne
        @JoinColumn(name = "PERFORMER_ID", referencedColumnName = "id")
        private Concert concert;

        public Performer() { }
    
        public Performer(Long id, String name, String imageUri, Genre genre) {
            this.id = id;
            this.name = name;
            this.imageUri = imageUri;
            this.genre = genre;
        }
    
        public Performer(String name, String imageUri, Genre genre) {
            this(null, name, imageUri, genre);
        }
    
        public Long getId() {
            return id;
        }
    
        public void setId(Long id) {
            this.id = id;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    
        public String getImageUri() {
            return imageUri;
        }
    
        public void setImageUri(String imageUri) {
            this.imageUri = imageUri;
        }
    
        public Genre getGenre() {
            return genre;
        }
    
        public void setGenre(Genre genre) {
            this.genre = genre;
        }
    
        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Performer, id: ");
            buffer.append(id);
            buffer.append(", name: ");
            buffer.append(name);
            buffer.append(", s3 image: ");
            buffer.append(imageUri);
            buffer.append(", genre: ");
            buffer.append(genre.toString());
    
            return buffer.toString();
        }
    
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Performer))
                return false;
            if (obj == this)
                return true;
    
            Performer rhs = (Performer) obj;
            return new EqualsBuilder().
                    append(name, rhs.name).
                    isEquals();
        }
    
        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 31).
                    append(name).hashCode();
        }
}
