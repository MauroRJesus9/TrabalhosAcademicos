using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace TPV2.Models
{
    public class PropertiesImages
    {
        [Key]
        [Required]
        public int PropImageId { get; set; }

        [Required]
        [StringLength(50)]
        public string FileName { get; set; }

        [ForeignKey("PropertyImage")]
        public int? PropertyID { get; set; }
        public Properties PropertyImage { get; set; }
    }
}
