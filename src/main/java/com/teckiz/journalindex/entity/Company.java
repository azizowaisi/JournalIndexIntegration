package com.teckiz.journalindex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Company entity - matches Symfony AppBundle\Entity\Company
 */
@Entity
@Table(name = "Company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "company_key", length = 255, nullable = false)
    private String companyKey;

    @Column(name = "name", length = 255, unique = true)
    private String name;

    @Column(name = "slug", length = 64, unique = true, nullable = false)
    private String slug;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "stripe_id", length = 255)
    private String stripeId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "aboutus", columnDefinition = "TEXT")
    private String aboutus;

    @Column(name = "city", length = 255)
    private String city;

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "time_zone", length = 255)
    private String timeZone;

    @Column(name = "is_active")
    private Boolean active = false;

    @Column(name = "is_archived")
    private Boolean archived = false;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 255)
    private String phone;

    @Column(name = "map_location")
    private String location;

    @Column(name = "map_coordinates")
    private String coordinates;

    @Column(name = "logo", columnDefinition = "TEXT")
    private String logo;

    @Column(name = "logo_size", columnDefinition = "TEXT")
    private String logoSize;

    @Column(name = "favicon", columnDefinition = "TEXT")
    private String favicon;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "workingdays", length = 255)
    private String workingdays;

    @Column(name = "holidays", length = 255)
    private String holidays;

    @Column(name = "privacy_policy", columnDefinition = "TEXT")
    private String privacyPolicy;

    @Column(name = "lang", length = 10)
    private String lang;

    @Column(name = "is_master")
    private Boolean master;

    // Relationships
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexJournal> journals = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexJournalSetting> journalSettings = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexJournalSubject> journalSubjects = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexLanguage> languages = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexCountry> countries = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexJournalArticle> articles = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexJournalVolume> volumes = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexJournalPage> pages = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IndexRelatedMedia> relatedMedia = new ArrayList<>();

    @OneToMany(mappedBy = "subCompany", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Company> subCompanies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_company_id")
    private Company masterCompany;

    // Constructors
    public Company() {
        this.companyKey = generateEntityKey();
    }

    // Utility method to generate entity key
    private String generateEntityKey() {
        return "COMP_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyKey() {
        return companyKey;
    }

    public void setCompanyKey(String companyKey) {
        this.companyKey = companyKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStripeId() {
        return stripeId;
    }

    public void setStripeId(String stripeId) {
        this.stripeId = stripeId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAboutus() {
        return aboutus;
    }

    public void setAboutus(String aboutus) {
        this.aboutus = aboutus;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getLogoSize() {
        return logoSize;
    }

    public void setLogoSize(String logoSize) {
        this.logoSize = logoSize;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getWorkingdays() {
        return workingdays;
    }

    public void setWorkingdays(String workingdays) {
        this.workingdays = workingdays;
    }

    public String getHolidays() {
        return holidays;
    }

    public void setHolidays(String holidays) {
        this.holidays = holidays;
    }

    public String getPrivacyPolicy() {
        return privacyPolicy;
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Boolean getMaster() {
        return master;
    }

    public void setMaster(Boolean master) {
        this.master = master;
    }

    // Relationship getters and setters
    public List<IndexJournal> getJournals() {
        return journals;
    }

    public void setJournals(List<IndexJournal> journals) {
        this.journals = journals;
    }

    public List<IndexJournalSetting> getJournalSettings() {
        return journalSettings;
    }

    public void setJournalSettings(List<IndexJournalSetting> journalSettings) {
        this.journalSettings = journalSettings;
    }

    public List<IndexJournalSubject> getJournalSubjects() {
        return journalSubjects;
    }

    public void setJournalSubjects(List<IndexJournalSubject> journalSubjects) {
        this.journalSubjects = journalSubjects;
    }

    public List<IndexLanguage> getLanguages() {
        return languages;
    }

    public void setLanguages(List<IndexLanguage> languages) {
        this.languages = languages;
    }

    public List<IndexCountry> getCountries() {
        return countries;
    }

    public void setCountries(List<IndexCountry> countries) {
        this.countries = countries;
    }

    public List<IndexJournalArticle> getArticles() {
        return articles;
    }

    public void setArticles(List<IndexJournalArticle> articles) {
        this.articles = articles;
    }

    public List<IndexJournalVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<IndexJournalVolume> volumes) {
        this.volumes = volumes;
    }

    public List<IndexJournalPage> getPages() {
        return pages;
    }

    public void setPages(List<IndexJournalPage> pages) {
        this.pages = pages;
    }

    public List<IndexRelatedMedia> getRelatedMedia() {
        return relatedMedia;
    }

    public void setRelatedMedia(List<IndexRelatedMedia> relatedMedia) {
        this.relatedMedia = relatedMedia;
    }

    public List<Company> getSubCompanies() {
        return subCompanies;
    }

    public void setSubCompanies(List<Company> subCompanies) {
        this.subCompanies = subCompanies;
    }

    public Company getMasterCompany() {
        return masterCompany;
    }

    public void setMasterCompany(Company masterCompany) {
        this.masterCompany = masterCompany;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", companyKey='" + companyKey + '\'' +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", active=" + active +
                '}';
    }
}