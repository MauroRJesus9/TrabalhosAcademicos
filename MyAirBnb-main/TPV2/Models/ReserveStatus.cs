using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace TPV2.Models
{
    public class ReserveStatus
    {
        [Key]
        public int StatusId { get; set; }

        [Required]
        [StringLength(100)]
        public string Description { get; set; }

        [InverseProperty("Status")]
        public virtual ICollection<Reserves> Reserves { get; set; }

        [DefaultValue("false")]
        public bool isRemoved { get; set; }
    }
}

