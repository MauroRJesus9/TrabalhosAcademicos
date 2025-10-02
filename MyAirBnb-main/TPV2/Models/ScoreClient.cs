using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace TPV2.Models
{
    [Index(nameof(ReserveId), IsUnique = true)]
    public class ScoreClient
    {
        [Key]
        public int ScoreClientId { get; set; }

        [ForeignKey("SCClientID")]
        public string? ClientId { get; set; }
        public AplicationUser  SCClientID { get; set; }

        
        [ForeignKey("Reserves")]
        public int? ReserveId { get; set; }
        public Reserves Reserves { get; set; }
        [Required]
        [StringLength(250)]
        public string Comments { get; set; }
        [Required]
        public int ScorePropriedade { get; set; }
        [Required]
        public int ScoreProprietario { get; set; }

        [ForeignKey("SCProprietarioID")]
        public string? ProprietarioId { get; set; }
        public AplicationUser SCProprietarioID { get; set; }

        [Required]
        public int ScoreFuncionário { get; set; }

        [ForeignKey("SCFuncionarioID")]
        public string? FuncionarioId { get; set; }
        public AplicationUser SCFuncionarioID { get; set; }

        [DefaultValue("false")]
        public bool isRemoved { get; set; }

        [ForeignKey("PropertyScore")]
        public int? PropertyId { get; set; }
        public Properties PropertyScore { get; set; }

    }
}
