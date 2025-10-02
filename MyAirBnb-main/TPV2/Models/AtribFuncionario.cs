using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace TPV2.Models
{
    public class AtribFuncionario
    {
        [Required]
        public string UserId { get; set; }

        [Required]
        public string UserName { get; set; }
    }
}
