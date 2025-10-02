using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace TPV2.Models
{
    public class PropertyCategories
    {
        [Key]
        public int CategoryId { get; set; }
        [Required]
        [StringLength(50)]
        public string Description { get; set; }

        [InverseProperty("CProperty")]
        public virtual ICollection<Properties> Properties { get; set; }

        [DefaultValue("false")]
        public bool isRemoved { get; set; }
    }
}
