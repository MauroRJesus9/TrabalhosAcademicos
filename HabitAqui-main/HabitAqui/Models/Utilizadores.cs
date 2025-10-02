    using Microsoft.AspNetCore.Identity;
using System.ComponentModel.DataAnnotations;

namespace HabitAqui.Models{
    public class Utilizadores : IdentityUser{

        [Display(Name = "Nome Completo")]
        public string NomeCompleto { get; set; }

        public int NIF { get; set; }

        //Se for um Funcionario ou Gestor esta associado a uma empresa senao é null
        public int? EmpresaId { get; set; }
        public Empresa? Empresa { get; set; }

        //relacionamento com a entidade arrendamentos 1 para N
        public ICollection<Arrendamentos>? Arrendamentos { get; set; } // para saber quem criou esse arrendamento

        //se o user esta ativo ou não
        public bool Ativo { get; set; }

    }
}
