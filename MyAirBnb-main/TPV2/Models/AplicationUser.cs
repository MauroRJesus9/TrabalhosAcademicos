using Microsoft.AspNetCore.Identity;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace TPV2.Models
{
    public class AplicationUser : IdentityUser
    {

        //ID - String (AspNetUsers) vs. Int(ForeignKeys)

        //Reserves
        [InverseProperty("RClientIdReserve")]
        public virtual ICollection<Reserves> ReservesUsers { get; set; }

        //Properties
        [InverseProperty("PProprietarioID")]
        public virtual ICollection<Properties> PropertiesUsers { get; set; }
        [InverseProperty("PFuncionarioID")]
        public virtual ICollection<Properties> PropertiesFuncionario { get; set; }

        //ScoreGestor
        [InverseProperty("SGGestorID")]
        public virtual ICollection<ScoreGestor> ScoreGestorGestorID { get; set; }
        [InverseProperty("SGUserID")]
        public virtual ICollection<ScoreGestor> ScoreGestorUserID { get; set; }

        //ScoreClient
        [InverseProperty("SCClientID")]
        public virtual ICollection<ScoreClient> ScoreClientUserID { get; set; }
        [InverseProperty("SCProprietarioID")]
        public virtual ICollection<ScoreClient> ScoreClientProprietarioID { get; set; }
        [InverseProperty("SCFuncionarioID")]
        public virtual ICollection<ScoreClient> ScoreClientFuncionarioID { get; set; }
    }
}
