using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace TPV2.Models
{
    public class ScoreGestor
    {
        [Key]
        public int ScoreGestorId { get; set; }

        [ForeignKey("SGGestorID")]
        public string? GestorId { get; set; }
        public AplicationUser SGGestorID { get; set; }

        [ForeignKey("Reserves")]
        public int? ReserveId { get; set; }
        public Reserves Reserves { get; set; }

        [Required]
        [StringLength(250)]
        public string Comments { get; set; }

        [Required]
        public int ScoreClient { get; set; }


        [ForeignKey("SGUserID")]
        public string? ClientId { get; set; }
        public AplicationUser SGUserID { get; set; }

        [DefaultValue("false")]
        public bool isRemoved { get; set; }
    }
}
