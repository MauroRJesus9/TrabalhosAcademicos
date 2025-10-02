using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace TPV2.Models
{
    public class ModelImages
    {
        [Required]
        public string ImageFile { get; set; }
    }
}
