using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace TPV2.Models
{
    public class Properties
    {
        [Key]
        [Required]
        public int PropertyId { get; set; }

        [Required]
        [StringLength(50)]
        public string Name { get; set; }

        [Required]
        [StringLength(250)]
        public string Description { get; set; }

        [Required]
        public double Price { get; set; }

        [Required]
        [StringLength(100)]
        public string Address { get; set; }


        [ForeignKey("PProprietarioID")]
        public string? ProprietarioId { get; set; }
        public AplicationUser PProprietarioID { get; set; }

        
        [ForeignKey("PFuncionarioID")]
        public string? FuncionarioId { get; set; }
        public AplicationUser PFuncionarioID { get; set; }

        [ForeignKey("CProperty")]
        public int? CategoryId { get; set; }
        public PropertyCategories CProperty { get; set; }


        [InverseProperty("Property")]
        public virtual ICollection<Reserves> Reserves { get; set; }

        [DefaultValue("false")]
        public bool isRemoved { get; set; }

        [InverseProperty("PropertyImage")]
        public virtual ICollection<PropertiesImages> PropertyImag { get; set; }

        [InverseProperty("PropertyScore")]
        public virtual ICollection<ScoreClient> PropertySC { get; set; }


    }
}
