package asg.concert.service.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


@Entity
@Table(name = "Concerts")
public class Concert implements Comparable<Concert> {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String title;

        @Column(name="IMAGE_NAME")
        private String imageName;
        @Column(name="BLURB", length = 1000)
        private String blurb;
        
        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(
            name = "CONCERT_DATES",
            joinColumns = { @JoinColumn(name = "CONCERT_ID", referencedColumnName = "id") }
        )
        @Column(name="DATE")
        private Set<LocalDateTime> dates = new HashSet<>();
        @ManyToMany(targetEntity = Performer.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
        @JoinTable(
                name="CONCERT_PERFORMER",
                joinColumns = { @JoinColumn(name = "CONCERT_ID", referencedColumnName = "id") },
                inverseJoinColumns = { @JoinColumn(name = "PERFORMER_ID", referencedColumnName = "id") }
        )
        private Set<Performer> performers = new HashSet<>();
    
        public Concert() {
        }
    
        public Concert(Long id, String title, String imageName, String blurb) {
            this.id = id;
            this.title = title;
            this.imageName = imageName;
            this.blurb = blurb;
        }
    
        public Concert(String title, String imageName) {
            this.title = title;
            this.imageName = imageName;
        }
    
        public Long getId() {
            return id;
        }
    
        public void setId(Long id) {
            this.id = id;
        }
    
        public String getTitle() {
            return title;
        }
    
        public void setTitle(String title) {
            this.title = title;
        }
    
        public String getImageName() {
            return imageName;
        }
    
        public void setImageName(String imageName) {
            this.imageName = imageName;
        }
    
        public String getBlurb() {
            return blurb;
        }
    
        public void setBlurb(String blurb) {
            this.blurb = blurb;
        }
    
        public Set<LocalDateTime> getDates() {
            return dates;
        }
    
        public void setDates(Set<LocalDateTime> dates) {
            this.dates = dates;
        }
    
        public Set<Performer> getPerformers() {
            return performers;
        }
    
        public void setPerformers(Set<Performer> performers) {
            this.performers = performers;
        }

        @Override
        public boolean equals(Object obj) {
            // Implement value-equality based on a Concert's title alone. ID isn't
            // included in the equality check because two Concert objects could
            // represent the same real-world Concert, where one is stored in the
            // database (and therefore has an ID - a primary key) and the other
            // doesn't (it exists only in memory).
            if (!(obj instanceof Concert))
                return false;
            if (obj == this)
                return true;
    
            Concert rhs = (Concert) obj;
            return new EqualsBuilder().
                    append(title, rhs.title).
                    isEquals();
        }
    
        @Override
        public int hashCode() {
            // Hash-code value is derived from the value of the title field. It's
            // good practice for the hash code to be generated based on a value
            // that doesn't change.
            return new HashCodeBuilder(17, 31).
                    append(title).hashCode();
        }
    
        @Override
        public int compareTo(Concert concert) {
            return title.compareTo(concert.getTitle());
        }
}
