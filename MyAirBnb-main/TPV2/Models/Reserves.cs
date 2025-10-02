using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace TPV2.Models
{
    public class Reserves
    {
        [Key]
        public int ReserveId { get; set; }

        [Required]
        public DateTime CheckIn { get; set; }
        [Required]
        public DateTime CheckOut { get; set; }

        [ForeignKey("Property")]
        public int? PropertyId { get; set; }
        public Properties Property { get; set; }

        [Required]
        [StringLength(250)]
        public string Observations { get; set; }

        [ForeignKey("Status")]
        public int? StatusId { get; set; }
        public ReserveStatus Status { get; set; }

        [ForeignKey("RClientIdReserve")]
        public string? ClientId { get; set; }
        public AplicationUser RClientIdReserve { get; set; }

        [InverseProperty("Reserves")]
        public virtual ICollection<ScoreClient> ScoreClient { get; set; }

        [InverseProperty("Reserves")]
        public virtual ICollection<ScoreGestor> ScoreGestor { get; set; }

        [DefaultValue("false")]
        public bool isRemoved { get; set; }
    }
}
