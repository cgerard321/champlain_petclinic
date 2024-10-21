namespace emailing_service.Models.Visits
{
    using System;

    public class Visit
    {
        public Visit() { }
        public string Id { get; set; }
        public string VisitId { get; set; }
        public DateTime VisitDate { get; set; }
        public string Description { get; set; }
        public string PetId { get; set; }
        public string PractitionerId { get; set; }
        public Status Status { get; set; }
        public DateTime VisitEndDate { get; set; }
        public bool Reminder { get; set; }
        public string OwnerEmail { get; set; }
    }
}
